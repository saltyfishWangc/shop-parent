package com.wangc.config;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.wangc.job.FileCustomDataflowJob;
import com.wangc.job.FileCustomElasticJob;
import com.wangc.job.MyElasticJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author
 * @Description: 任务调度配置
 * @date 2022/10/13 20:17
 */
@Configuration
public class JobConfig {

    /**
     * 注入数据源，来配合JobEventConfiguration记录
     */
    @Autowired
    DataSource dataSource;

    /**
     * 注册中心配置
     * @param zookeeperUrl
     * @param zookeeperGroupName
     * @return
     */
    @Bean
    public CoordinatorRegistryCenter registryCenter(@Value("${elasticjob.zookeeper-url}") String zookeeperUrl, @Value("${elasticjob.group-name}") String zookeeperGroupName) {
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(zookeeperUrl, zookeeperGroupName);
        // 设置节点超时时间
        zookeeperConfiguration.setSessionTimeoutMilliseconds(100);
        CoordinatorRegistryCenter registryCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        registryCenter.init();
        return registryCenter;
    }

    /**
     * 任务调度配置
     * @param clazz 表示定时任务指定的业务类的字节码对象
     * @param cron  cron表达式
     * @param shardingCount 分片数量。表示任务由几台机器跑，如果配置为1，那么只会有一台机器在跑定时，当这个服务下线时，就会由另一台服务跑定时任务
     * @param shardingParameters 用于分片的条件
     *                           比如sgardingCount=4设置分片数量为4，那shardingParameters="0=text,1=iamge,2=radio,3=verdio"中0、1、2、3分别表示
     *                           分片索引，0=text表示第一个分片带着的参数为text，那么在定时任务业务执行时可以从shardingContext.getShardingParameter()
     *                           获取到这个传给当前分片的参数，业务代码里面就可以用这个参数去筛选对应的数据。这样就做到了业务数据分片，每个服务跑定时任务
     *                           时做到数据隔离，互不打扰
     * @return
     */
    private LiteJobConfiguration createJobConfiguration(Class clazz, String cron, int shardingCount, String shardingParameters, boolean isDataflowJob) {
        JobCoreConfiguration.Builder jobBuilder = JobCoreConfiguration.newBuilder(clazz.getSimpleName(), cron, shardingCount);
        if (!StringUtils.isEmpty(shardingParameters)) {
            jobBuilder.shardingItemParameters(shardingParameters);
        }
        // 定义作业核心配置
        JobCoreConfiguration coreConfig = jobBuilder.build();

        JobTypeConfiguration jobConfiguration = null;
        if (isDataflowJob) {
            // 定义Dataflow类型配置
            jobConfiguration = new DataflowJobConfiguration(coreConfig, clazz.getCanonicalName(), true);
        } else {
            // 定义SIMPLE类型配置，也就是指定定时任务指定的业务类
            jobConfiguration = new SimpleJobConfiguration(coreConfig, clazz.getCanonicalName());
        }

        // 定义Lite作业根配置，overwrite这个一定要有，否则定时任务配置要是有了变更不会同步到zookeeper
        LiteJobConfiguration simpleJobRootConfig = LiteJobConfiguration.newBuilder(jobConfiguration).overwrite(true).build();
        return simpleJobRootConfig;
    }

    /**
     * 创建MyElasticJob调度任务
     * @param job
     * @param registryCenter
     * @return
     */
    @Bean(initMethod = "init")
    public SpringJobScheduler springJobScheduler(MyElasticJob job, CoordinatorRegistryCenter registryCenter) {
        LiteJobConfiguration jobConfiguration = createJobConfiguration(job.getClass(), "0/5 * * * * ?", 1, null, false);
        return new SpringJobScheduler(job, registryCenter, jobConfiguration);
    }

    /**
     * 创建FileCustomElasticJob调度任务
     *
     * 注意：这个方法名不能用fileCustomElasticJob，因为FileCustomElasticJob本身注入到Spring容器时名字就是fileCustomElasticJob，
     * 这里方法在用这个名字的话，那这里创建的bean的名字也是fileCustomElasticJob了
     * @param job
     * @param registryCenter
     * @return
     */
    @Bean(initMethod = "init")
    public SpringJobScheduler testFileCustomElasticJob(FileCustomElasticJob job, CoordinatorRegistryCenter registryCenter) {
        LiteJobConfiguration jobConfiguration = createJobConfiguration(job.getClass(), "0/1 * * * * ?", 4, "0=text,1=iamge,2=radio,3=verdio", false);
        return new SpringJobScheduler(job, registryCenter, jobConfiguration);
    }

    /**
     * 创建FileCustomDataflowJob调度任务
     * @param job
     * @param registryCenter
     * @return
     */
    @Bean(initMethod = "init")
    public SpringJobScheduler testFileCustomDataflowJob(FileCustomDataflowJob job, CoordinatorRegistryCenter registryCenter) {
        LiteJobConfiguration jobConfiguration = createJobConfiguration(job.getClass(), "0/5 * * * * ?", 1, null, true);

        /**
         * 增加任务事件追踪配置
         * 这样任务的执行情况就被记录在数据库
         */
        JobEventConfiguration jobEventConfiguration = new JobEventRdbConfiguration(dataSource);

//        return new SpringJobScheduler(job, registryCenter, jobConfiguration);
        return new SpringJobScheduler(job, registryCenter, jobConfiguration, jobEventConfiguration);
    }
}
