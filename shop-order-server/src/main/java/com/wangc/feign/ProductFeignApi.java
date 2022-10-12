package com.wangc.feign;

import com.wangc.domain.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author
 * @Description: fallback = ProductFeignFallBack.class是在调用下游服务出问题时返回默认的调用结果
 * @date 2022/10/12 14:47
 */
@FeignClient(name = "product-service", fallback = ProductFeignFallBack.class)
public interface ProductFeignApi {

    @RequestMapping("/product/${pid}")
    Product findByPid(@PathVariable("pid") Long pid);
}
