package com.wangc.controller;

import com.alibaba.fastjson.JSON;
import com.wangc.domain.Product;
import com.wangc.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 * @Description:
 * @date 2022/10/11 20:40
 */
@RestController("/product")
@Slf4j
public class ProductController {

    @Autowired
    IProductService productService;

    @RequestMapping("/{pid}")
//    public Product findById(@PathVariable("pid") Long pid) {
    public Product findById(@PathVariable("pid") String pid) {
        log.info("接下来要进行{}号商品信息的查询", pid);
        Product product = productService.findByPid(Long.valueOf(pid));
        log.info("商品信息查询成功，内容为{}", JSON.toJSONString(product));
        return product;
    }
}
