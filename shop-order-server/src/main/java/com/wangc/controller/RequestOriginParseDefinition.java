package com.wangc.controller;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author
 * @Description: 用于授权规则中解析请求参数的组件
 * @date 2022/10/12 19:38
 */
@Component
public class RequestOriginParseDefinition implements RequestOriginParser {

    @Override
    public String parseOrigin(HttpServletRequest httpServletRequest) {
        // 按照接口双方约定的在请求中去获取授权信息，如果约定是在请求头的某个字段，那这里就是在请求头获取
        String type = httpServletRequest.getParameter("type");
        return type;
    }
}
