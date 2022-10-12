package com.wangc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @author
 * @Description:
 * @date 2022/10/11 20:52
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // 扫描当前包及其子包中被@FeignClient标注的接口
public class OrderServer {

    public static void main(String... args) {
        SpringApplication.run(OrderServer.class, args);
    }

    @Bean
    @LoadBalanced // 集成Ribbon进行负载均衡
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
