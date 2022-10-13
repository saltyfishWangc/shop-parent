package com.wangc.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @Description: 自定义局部网关过滤器实现耗时打印
 * 配置中添加：
 * - Time=true
 * @date 2022/10/13 9:54
 */
@Component
@Slf4j
public class TimeGatewayFilterFactory extends AbstractGatewayFilterFactory<TimeGatewayFilterFactory.Config> {

    public TimeGatewayFilterFactory() {
        super(Config.class);
    }

    /**
     * 将配置中- Time=true的配置值和Config的字段对应起来
     *
     * 如果Config内有show、me、ik三个属性，那么配置则是- Time=true,2,dd，这个方法的逻辑就应该是Arrays.asList("show", "me", "ik")
     * 那么就会将true,2,add分别赋值给Config.show、Config.me、Config.ik
     * @return
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("show");
    }

    /**
     * 拦截到之后就会调用apply方法，把创建对象时候反射创建出来的Config传入进来
     * @param config
     * @return
     */
    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                /**
                 * 如果只有前置逻辑代码应该如下：
                 *
                 * {前置逻辑}
                 * return chain.filter(exchange);
                 */
                if (!config.show) {
                    return chain.filter(exchange);
                }
                log.info("前置逻辑");
                long start = System.currentTimeMillis();
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    // 后置的逻辑
                    log.info("后置逻辑");
                    log.info("本次请求耗时：{}s", (System.currentTimeMillis() - start)/1000);
                }));
            }
        };
    }

    @Getter
    @Setter
    static class Config {
        private boolean show;
    }
}
