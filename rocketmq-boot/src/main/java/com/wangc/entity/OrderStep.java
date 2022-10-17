package com.wangc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author
 * @Description:
 * @date 2022/10/17 19:59
 */
@Data
@AllArgsConstructor
public class OrderStep {

    private long orderId;

    private String desc;
}
