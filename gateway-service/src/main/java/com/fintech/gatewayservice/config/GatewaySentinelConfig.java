package com.fintech.gatewayservice.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 网关限流（Sentinel + Spring Cloud Gateway adapter）。
 *
 * 1. 注册 SentinelGatewayFilter 拦截网关请求
 * 2. 注册 SentinelGatewayBlockExceptionHandler 处理被限流请求
 * 3. @PostConstruct 中配置自定义 429 响应 + 各路由 QPS 限流规则
 */
@Configuration
public class GatewaySentinelConfig {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public GatewaySentinelConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                 ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    @Bean
    @Order(-1)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    @PostConstruct
    public void init() {
        // 1. 自定义限流响应，对齐 Result 结构
        GatewayCallbackManager.setBlockHandler((exchange, t) -> {
            String body = "{\"code\":429,\"msg\":\"请求太频繁，请稍后再试\",\"data\":null}";
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        });

        // 2. 各路由限流规则（resource = application.yml 里 routes 的 id）
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("user-service").setCount(10).setIntervalSec(1));    // 10 QPS
        rules.add(new GatewayFlowRule("product-service").setCount(10).setIntervalSec(1));
        rules.add(new GatewayFlowRule("order-service").setCount(5).setIntervalSec(1));    // 5 QPS（演示好触发）
        rules.add(new GatewayFlowRule("ai-chat-service").setCount(5).setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);
    }
}
