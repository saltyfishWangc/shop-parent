package com.wangc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author
 * @Description:
 * @date 2022/10/11 20:27
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductServer {

    public static void main(String... args) {
        SpringApplication.run(ProductServer.class, args);
    }
}
