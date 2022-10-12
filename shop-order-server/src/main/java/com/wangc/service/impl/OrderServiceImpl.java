package com.wangc.service.impl;

import com.alibaba.fastjson.JSON;
import com.wangc.dao.OrderDao;
import com.wangc.domain.Order;
import com.wangc.domain.Product;
import com.wangc.feign.ProductFeignApi;
import com.wangc.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author
 * @Description:
 * @date 2022/10/11 20:56
 */
@Service
@Slf4j
public class OrderServiceImpl implements IOrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    ProductFeignApi productFeignApi;

    AtomicInteger i = new AtomicInteger();

    @Override
    public Order createOrder(Long productId, Long userId) {
        log.info("接收到{}号商品的下单请求，接下来调用商品微服务查询此商品信息", productId);
        // 远程调用商品微服务，查询商品信息
        // 引入nacos后，可以拿到远程服务实例
//        ServiceInstance serviceInstance = discoveryClient.getInstances("product-service").get((discoveryClient.getInstances("product-service")).size()%i.get());
//        i.incrementAndGet();
//        String host = serviceInstance.getHost();
//        int port = serviceInstance.getPort();
//        String url = "http://" + host + ":" + port + "/product/" + productId;
//        String url = "http://product-service/product/" + productId;
//        // 通过RestTemplate调用
//        Product product = restTemplate.getForObject(url, Product.class);
        // 通过feign调用
        Product product = productFeignApi.findByPid(productId);
        log.info("查询到{}号商品的信息，内容是:{}", productId, JSON.toJSONString(product));
        // 创建订单并保存
        Order order = new Order();
        order.setUid(userId);
        order.setUsername("wangc");
        order.setPid(productId);
        order.setPname(product.getPname());
        order.setPprice(product.getPprice());
        order.setNumber(1);
        orderDao.save(order);
        log.info("创建订单成功，订单信息为:{}", JSON.toJSONString(order));
        return order;
    }
}
