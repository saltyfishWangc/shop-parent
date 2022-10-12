package com.wangc.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author
 * @Description:
 * @date 2022/10/12 17:57
 */
@Service
@Slf4j
public class TraceServiceImpl {

    // 表示这是一个Sentinel链路资源
    @SentinelResource(value = "traceService")
    public void traceService() {
        log.info("调用traceService方法");
    }
}
