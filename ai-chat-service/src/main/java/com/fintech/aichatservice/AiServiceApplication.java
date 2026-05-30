package com.fintech.aichatservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.fintech")
@EnableFeignClients(basePackages = "com.fintech")   // Feign 客户端在 com.fintech.client，需显式指定
@MapperScan("com.fintech.history.mapper")
public class AiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
