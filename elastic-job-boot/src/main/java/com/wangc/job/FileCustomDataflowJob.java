package com.wangc.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.wangc.domain.FileCustom;
import com.wangc.mapper.FileCustomMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author
 * @Description:
 * @date 2022/10/14 16:48
 */
@Component
@Slf4j
public class FileCustomDataflowJob implements DataflowJob<FileCustom> {

    @Autowired
    FileCustomMapper fileCustomMapper;

    /**
     * 这个方法是抓取数据
     * 定时任务启动时会自动调用这个方法，如果这个方法返回有值，才会调用processData()，并且返回值作为processData()第二个参数的值传入
     * @param shardingContext
     * @return
     */
    @Override
    public List<FileCustom> fetchData(ShardingContext shardingContext) {
        log.info("开始抓取数据-----");
        List<FileCustom> fileCustoms = fileCustomMapper.selectByLimit(2);
        return fileCustoms;
    }

    /**
     * 处理数据
     * @param shardingContext
     * @param list
     */
    @Override
    public void processData(ShardingContext shardingContext, List<FileCustom> list) {
        log.info("开始处理数据-----");
        list.forEach(data -> {
            backUp(data);
        });
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
