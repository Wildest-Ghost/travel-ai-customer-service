package com.fintech.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * user-service 启动类。
 *
 * 关键点：scanBasePackages = "com.fintech"
 * - 默认 @SpringBootApplication 只扫描启动类同包及子包，即 com.fintech.userservice.*
 * - 但 common 模块的 GlobalExceptionHandler 在 com.fintech.common 包里 —— 不在子包
 * - 把扫描范围抬到 com.fintech，就能同时覆盖 common 和 userservice
 *
 * 服务发现：classpath 上有 spring-cloud-starter-alibaba-nacos-discovery 时，
 * Spring Cloud 会自动启用 DiscoveryClient，无需 @EnableDiscoveryClient
 * （新版 Spring Cloud 推荐做法）。
 */
@SpringBootApplication(scanBasePackages = "com.fintech")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
