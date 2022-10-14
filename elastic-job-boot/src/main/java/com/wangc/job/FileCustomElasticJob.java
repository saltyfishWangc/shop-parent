package com.wangc.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.wangc.domain.FileCustom;
import com.wangc.mapper.FileCustomMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @Description:
 * @date 2022/10/14 15:57
 */
@Component
@Slf4j
public class FileCustomElasticJob implements SimpleJob {

    @Autowired
    FileCustomMapper fileCustomMapper;

    @Override
    public void execute(ShardingContext shardingContext) {
        long threadID = Thread.currentThread().getId();
        log.info("线程ID:{}，任务名称:{}，任务参数:{}，分片个数:{}，分片索引号:{}，分片参数:{}",
                threadID,
                shardingContext.getJobName(),
                shardingContext.getJobParameter(),
                shardingContext.getShardingTotalCount(),
                shardingContext.getShardingItem(),
                shardingContext.getShardingParameter()
        );
        doWork(shardingContext.getShardingParameter());
    }

    private void doWork(String shardingParameter) {
        List<FileCustom> fileCustoms = fileCustomMapper.selectByType(shardingParameter);
        for (FileCustom fileCustom : fileCustoms) {
            backUp(fileCustom);
        }
    }

    private void backUp(FileCustom fileCustom) {
        log.info("备份的方法名:{}，备份的类型:{}", fileCustom.getName(), fileCustom.getType());
        log.info("==========================");
        try {
            TimeUnit.SECONDS.sleep(1);
            fileCustomMapper.updateBackUpById(fileCustom.getId(), fileCustom.getBackedUp());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
