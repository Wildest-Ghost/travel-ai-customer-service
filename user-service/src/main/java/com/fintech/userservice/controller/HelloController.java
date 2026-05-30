package com.fintech.userservice.controller;

import com.fintech.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查端点，用于确认服务存活。
 */
@RestController
public class HelloController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success(appName + " is alive");
    }
}
