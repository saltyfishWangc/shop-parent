package com.wangc.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author
 * @Description: 配合Sentinel做热点流控的演示类
 * @date 2022/10/12 19:21
 */
@RestController
@Slf4j
public class HotSpotController {

    @RequestMapping("/hotSpot1")
    @SentinelResource(value = "hotSpot1")
    public String hotSpot(Long productId) {
      log.info("访问编号为:{}的商品", productId);
      return "hotspot1";
    }
}
