package com.fintech.userservice.dto;

import lombok.Data;

/**
 * 对外返回的用户视图。
 * 关键：不含 password —— GET /users/{id} 返回它，不会泄露密码 hash。
 */
@Data
public class UserVO {
    private Long id;
    private String username;
}
