package com.wangc.controller;

import com.wangc.domain.Order;
import com.wangc.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 * @Description:
 * @date 2022/10/11 21:03
 */
@RestController("/order")
@Slf4j
public class OrderController {

    @Autowired
    IOrderService orderService;

    @RequestMapping("/order")
    public Order order(Long pid, Long uid) {
        return orderService.createOrder(pid, uid);
    }
}
