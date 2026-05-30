package com.fintech.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "userId 不能为空")
    private Long userId;

    @NotNull(message = "productId 不能为空")
    private Long productId;

    @NotNull
    @Positive(message = "数量必须 > 0")
    private Integer quantity;

    private String contactName;
    private String contactPhone;
}
