package com.wangc.service;

import com.wangc.domain.Order;

public interface IOrderService {

    Order createOrder(Long productId, Long userId);
}
