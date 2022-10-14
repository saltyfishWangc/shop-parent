package com.wangc.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 * @Description: 配合Nacos Config动态刷新的演示类
 * @date 2022/10/13 16:08
 */
@RestController
@RefreshScope
public class NacosDynamicConfigTestController {

    @Value("${appConfig.name}")
    private String appConfigName;

    @RequestMapping("/hello")
    public String hello() {
        return "参数内容:" + appConfigName;
    }
}
