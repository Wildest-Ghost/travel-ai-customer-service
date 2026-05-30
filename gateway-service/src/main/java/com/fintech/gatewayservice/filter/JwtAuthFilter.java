package com.fintech.gatewayservice.filter;

import com.fintech.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关统一 JWT 校验过滤器（GlobalFilter，对所有路由生效）。
 *
 * 流程：
 *  1. 白名单（注册/登录）直接放行
 *  2. 取 Authorization: Bearer xxx；缺失/格式错 → 401
 *  3. JwtUtil 校验签名 + 过期；失败 → 401
 *  4. 解析 userId/username，塞进请求头 X-User-Id / X-User-Name 透传给下游
 *
 * 下游服务【信任】这两个头，不再自己校验 token —— 这就是"网关统一鉴权 + 下游信任"模式。
 *
 * 注意：Spring Cloud Gateway 是 WebFlux（reactive），返回 Mono<Void>，
 * 写响应体和普通 servlet 不一样。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    /** 放行清单：这些路径不需要 token */
    private static final List<String> WHITELIST = List.of(
            "/users/register",
            "/users/login"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 0. CORS 预检请求（OPTIONS）直接放行 —— 它不带 token，否则浏览器跨域会被这里拦成 401
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 1. 白名单放行
        if (WHITELIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 2. 取 token
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return unauthorized(exchange, "缺少 token");
        }
        String token = auth.substring(7);

        // 3. 校验
        if (!jwtUtil.validate(token)) {
            return unauthorized(exchange, "token 无效或已过期");
        }

        // 4. 解析身份，透传给下游
        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id", userId == null ? "" : String.valueOf(userId))
                .header("X-User-Name", username == null ? "" : username)
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    /** 早于路由转发执行；数字越小优先级越高 */
    @Override
    public int getOrder() {
        return -100;
    }

    /** 返回 401，body 对齐 Result 结构 {"code":401,"msg":...,"data":null} */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
