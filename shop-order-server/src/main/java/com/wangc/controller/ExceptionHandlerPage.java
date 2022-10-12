package com.wangc.controller;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author
 * @Description: 自定义Sentinel异常返回
 * 注意：这个是全局的，当某个服务接口自己被@SentinelResource定义了异常返回后，会以它自己的为准
 * @date 2022/10/12 19:53
 */
@Component
public class ExceptionHandlerPage implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        ResultData data = null;
        if (e instanceof FlowException) {
            data = new ResultData(-1, "接口被限流了");
        } else if (e instanceof DegradeException) {
            data = new ResultData(-2, "接口被降级了");
        } else if (e instanceof ParamFlowException) {
            data = new ResultData(-3, "参数限流异常了");
        } else if (e instanceof AuthorityException) {
            data = new ResultData(-4, "授权异常");
        } else if (e instanceof SystemBlockException) {
            data = new ResultData(-5, "接口被系统规则降级了");
        }
        response.getWriter().write(JSON.toJSONString(data));
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class ResultData {
    private int code;
    private String message;
}
