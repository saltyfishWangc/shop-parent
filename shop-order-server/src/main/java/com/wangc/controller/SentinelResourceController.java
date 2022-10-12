package com.wangc.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 * @Description: 体验Sentinel的@SentinelResouce用法的演示类
 * 总结：
 * @SentinelResource的异常返回优先级高于自定义的继承BlockExceptionHandler的组件类
 * @date 2022/10/12 20:10
 */
@RestController
@Slf4j
public class SentinelResourceController {

    @RequestMapping("/sentinelResource")
    @SentinelResource(value = "sentinelResource",
            blockHandler = "sentinelResourceBlockHandler", // 当前方法如果被限流或者被降级会调用这个字符串对应的方法
            fallback = "sentinelResourceFallBack" // 当方法返回异常时调用这个字符串对应的方法
    )
    public String sentinelResource(String name) {
        if ("wangc".equals(name)) {
            throw new RuntimeException();
        }
        return "anno";
    }

    public String sentinelResourceBlockHandler(String name, BlockException ex) {
        log.error("{}", ex);
        return "接口被限流或者降级了";
    }

    // Throwable时也就是降级时进入的方法
    public String sentinelResourceFallBack(String name, Throwable throwable) {
        log.error("{}", throwable);
        return "接口发生异常了";
    }
}
