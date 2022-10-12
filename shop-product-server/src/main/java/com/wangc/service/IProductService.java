package com.wangc.service;

import com.wangc.domain.Product;

public interface IProductService {

    Product findByPid(Long pid);
}
