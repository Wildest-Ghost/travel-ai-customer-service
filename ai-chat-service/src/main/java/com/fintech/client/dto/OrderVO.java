package com.fintech.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;          // 后续可以再 Feign 拉产品名做拼接
    private String status;           // PENDING / PAID / CANCELLED / CHANGED / REFUNDED
    private BigDecimal amount;
    private Integer quantity;
    private LocalDateTime startTime; // 出行开始（起飞 / 入住）
    private LocalDateTime endTime;
}
