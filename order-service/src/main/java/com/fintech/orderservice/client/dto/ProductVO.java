package com.fintech.orderservice.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Feign 反序列化用的 product-service 返回结构。
 * 创建订单时主要用 price / startTime / endTime。
 */
@Data
public class ProductVO {
    private Long id;
    private String type;
    private String name;
    private BigDecimal price;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
