package com.fintech.orderservice.client.dto;

import lombok.Data;

/**
 * Feign 反序列化用的 user-service 返回结构。
 * 故意只放必要字段（id/username），Jackson 默认忽略多余字段。
 */
@Data
public class UserVO {
    private Long id;
    private String username;
}
