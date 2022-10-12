package com.wangc.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author
 * @Description:
 * @date 2022/10/11 20:47
 */
@Entity(name = "t_shop_order")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long oid; //订单id

    private Long uid; //用户id

    private String username; //用户名

    private Long pid; //商品id

    private String pname; //商品名称

    private Double pprice; //商品单价

    private Integer number; //购买数量
}
