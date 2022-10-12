package com.wangc.controller;

import com.wangc.service.impl.TraceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 * @Description: 链路流控的演示类
 * @date 2022/10/12 17:56
 */
@RestController
public class TraceController {

    @Autowired
    private TraceServiceImpl traceService;

    /**
     * /trace1/traceService 形成链路
     * 如果没有配置web-context-unify: false，那么就只有traceService
     * @return
     */
    @RequestMapping("/trace1")
    public String trace1() {
        traceService.traceService();
        return "trace1";
    }

    /**
     * /trace2/traceService 形成链路
     * 如果没有配置web-context-unify: false，那么就只有traceService
     * @return
     */
    @RequestMapping("/trace2")
    public String trace2() {
        traceService.traceService();
        return "trace2";
    }
}
