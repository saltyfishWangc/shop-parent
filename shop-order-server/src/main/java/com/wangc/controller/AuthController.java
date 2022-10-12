package com.wangc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 * @Description: 配合Sentinel的授权规则的演示类
 * 同时，还有自定义的请求参数解析组件类：com.wangc.controller.RequestOriginParseDefinition
 * @date 2022/10/12 19:41
 */
@RestController
@Slf4j
public class AuthController {

    /**
     * 注意：这个方法的参数名要和自定义的RequestOriginParser字类组件类parseOrigin里面返回的字段名一致
     * @param type
     * @return
     */
    @RequestMapping("/auth")
    public String auth(String type) {
        log.info("应用:{}访问接口", type);
        return "auth";
    }
}
