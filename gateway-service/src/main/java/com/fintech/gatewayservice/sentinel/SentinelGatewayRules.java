package com.fintech.gatewayservice.sentinel;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashSet;
import java.util.Set;

@Component
public class SentinelGatewayRules {
    @PostConstruct
    public void init() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        // resource = 路由 id（和 application.yml 里的 routes id 对应）
        rules.add(new GatewayFlowRule("order-service").setCount(5).setIntervalSec(1)); // 5 QPS
        rules.add(new GatewayFlowRule("user-service").setCount(10).setIntervalSec(1));
        // ... product / 其他
        GatewayRuleManager.loadRules(rules);
    }
    @PostConstruct
    public void initBlockHandler() {
        GatewayCallbackManager.setBlockHandler((exchange, t) -> {
            String body = "{\"code\":429,\"msg\":\"请求太频繁，请稍后再试\",\"data\":null}";
            return ServerResponse.status(429)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        });
    }
}
