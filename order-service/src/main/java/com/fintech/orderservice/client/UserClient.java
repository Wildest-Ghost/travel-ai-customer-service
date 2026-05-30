package com.fintech.orderservice.client;

import com.fintech.common.Result;
import com.fintech.orderservice.client.dto.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 通过 Nacos 服务名 user-service 解析到实例，发起 HTTP 调用。
 * 注意：user-service 必须有 GET /users/{id} 接口，否则这里会 404。
 */
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/{id}")
    Result<UserVO> findById(@PathVariable("id") Long id);
}
