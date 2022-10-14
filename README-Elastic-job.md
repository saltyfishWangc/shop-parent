### 任务调度
任务调度是为了自动完成特定任务，在约定的特定时刻去执行任务的过程。也就是定时任务。
在Spring中，使用@Scheduled去标识一个方法是定时执行，然后在启动类上加上@EnableScheduling注解。
例如：
```
@Scheduled(cron = "0/20 * * * * ? ")
public void doWork() {
    // do something
}
```

### 分布式调度
上面的任务调度在服务只是单台时没问题，但是当服务是集群部署时，里面的业务就会重复执行。
需要用到分布式调度的原因：
1. 单机处理极限：原本1分钟内需要处理1万个订单，但是现在需要1分钟处理10万订单；原来一个统计需要1小时，现在也无妨需要10分钟就统计出来。
当然，也可以单机多线程处理。但是单机能力毕竟有限(CPU、内存、磁盘这些资源)，始终会有单机处理不过来的情况。
2. 高可用：单机版的定时任务调度只能在一台机器上运行，如果程序或者系统出现异常就会导致功能不可用，虽然可以在单机程序实现的足够稳定，但
始终有机会遇到非程序引起的故障，而这个对于一个系统的核心功能来说是不可以接受的。
3. 防止重复执行：在单机模式下，定时任务是没问题的，但当我们部署了多台服务，同时每台服务有定时任务时，若不进行合理的控制在同一时间，只
有一个定时任务启动执行，这时，定时任务的结果可能存在混乱和错误。
基于上面的问题，就需要分布式任务调度来解决。

#### Elastic-job
见elastic-job-boot模块

文档地址：https://shardingsphere.apache.org/elasticjob/current/cn/overview/
可以参考文档单独起个工程试下

需要启动zookeeper:
下载地址：http://archive.apache.org/dist/zookeeper/zookeeper-3.4.1/zookeeper-3.4.1.tar.gz

#### Spring Boot整合Elastic-job

##### 快速入门
1. 引入依赖。elastic-job没有提供starter
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-spring</artifactId>
    <version>${elastic-job-version}</version>
</dependency>
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-core</artifactId>
    <version>${elastic-job-version}</version>
</dependency>
<!-- 因为elastic-job-2.1.5的依赖下curator、guava版本过高，导致程序启动时报如下错 -->
<!--
    om.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter.init(ZookeeperRegistryCenter.java:72)
    Caused by: java.lang.NoClassDefFoundError: org/apache/curator/connection/ConnectionHandlingPolicy
-->
<!-- 需要引入以下版本 -->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>2.10.0</version>
</dependency>
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
    <version>2.10.0</version>
</dependency>
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>18.0</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

2. 编写定时任务类
实现com.dangdang.ddframe.job.api.simple.SimpleJob，重载execute()方法
代码见：com.wangc.job.MyElasticJob

3. 编写配置类
代码实现见：com.wangc.config.JobConfig

4. 注册中心配置
因为不是starter，这个配置名随便定义，和代码里面保持一致即可。
```
elasticjob:
  zookeeper-url: 192.168.19.128:2181
  group-name: elastic-job-group
```

##### 分片
1. 引入持久层依赖
```
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.10</version>
</dependency>
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>1.2.0</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.47</version>
</dependency>
```

2. 代码实现分片配置
所谓分片就是给某个定时任务配置一个分片数量，告诉elastic-job调度，
这个定时任务可以开启多少个线程，至于这些线程是都在一台服务上，还是分摊在每个服务商，这是
elastic-job的策略，如果有新增的服务上线了或者有服务下线了，elastic-job会在下一次定时任务
执行时分配好。比如，我们指定分片数量为4，现在只有一个服务在跑，那么elastic-job就会在这个
服务上创建4个线程去执行任务。现在我们新增了一个服务，那在该该定时任务下一次执行时，elastic-job
会把这两个服务都用起来，分别在这两个服务上创建两个线程去执行定时任务。同理，有一台服务下线了，那么
在该定时任务下一个执行时，elastic-job会根据分配配置以及当前服务存活的情况来分配，那就是在活着的
那个服务器启动4个线程来执行定时任务。
那么，这些任务并发执行时业务数据不会交叉吗？
不会，因为当分片数量配置超过1时，就会让我们配置分片参数，也就是每个线程去执行任务时带过去的参数，这个参数
就是我们定时任务业务代码里面要拿来去作为筛选业务数据的条件的。
其实还有个问题，如果我们给每个分片配置的参数都是一样的，那不是又回到了这个问题吗？
<b>这个问题后面用案例试下</b>

分片配置代码实现见定时任务：com.wangc.config.JobConfig.testFileCustomElasticJob

##### Dataflow类型调度任务
Dataflow类型的定时任务需要实现DataflowJob接口，该接口提供2个方法供覆盖，分别用于抓取(fetchData)和处理(processData)数据。
Dataflow类型用于处理数据流，和SimpleJob不同，它以数据流的方式执行，调用fetchData抓取数据，直到抓取不到数据才停止作业。

其实数据流处理这种概念在很多地方都会见到，总结了下什么时候会想到用数据流：那就是数据量很大，如果一下子加载可能会撑爆内存，所以
才用数据流的方式。就像SimpleJob接口，依赖于我们的查数脚本，如果sql脚本没有分页，那就是全部查出来了。

Dataflow类型调度任务代码时间见：com.wangc.config.JobConfig.testFileCustomDataflowJob
在一个定时周期内，fetchData方法会一直去抓，抓到数据就丢给processData方法去处理，直到没抓到数据，那这个周期内定时任务就停了。
下一个定时周期又同样从fetchData方法开始。

那有个问题，如果在一个定时周期内，数据量大到每次抓取都有数据，那还会开启下一个周期吗？
