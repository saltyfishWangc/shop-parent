package com.wangc.feign;

import com.wangc.domain.Product;
import org.springframework.stereotype.Component;

/**
 * @author
 * @Description: 这是Feign整合Sentinel时，当下游服务被流控时，默认走的fallback的一个兜底返回。
 * 因为在有的业务中下游服务出现问题时，不至于直接将整个业务判死刑
 * @date 2022/10/12 20:29
 */
@Component
public class ProductFeignFallBack implements ProductFeignApi{

    @Override
    public Product findByPid(Long pid) {
        Product product = new Product();
        product.setPid(-1L);
        product.setPname("兜底返回");
        product.setPprice(0.0);
        return product;
    }
}
