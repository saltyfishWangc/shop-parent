package com.wangc.service.impl;

import com.wangc.dao.ProductDao;
import com.wangc.domain.Product;
import com.wangc.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author
 * @Description:
 * @date 2022/10/11 20:38
 */
@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    public Product findByPid(Long pid) {
        return productDao.findById(pid).get();
    }
}
