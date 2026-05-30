package com.fintech.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发 + 校验。
 * 放在 common-security 模块，user-service（登录时签发）和 gateway（每请求校验）共用。
 *
 * 设计：claim 装 userId + username；过期 12 小时；HS256 对称签名，secret 来自配置 jwt.secret。
 * secret 放配置而非硬编码：签发方与校验方必须使用同一 secret，统一管理便于后续迁移到配置中心。
 */
@Component
public class JwtUtil {

    /** 签名密钥（由配置里的 jwt.secret 构造） */
    private final SecretKey key;

    /** 过期时间（毫秒） */
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:43200000}") long expirationMs) {  // 默认 43200000ms = 12 小时
        // HS256 要求密钥至少 32 字节（256 bit），过短会抛 WeakKeyException
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** 签发 token：登录校验密码通过后调用。claim 里同时装 userId 和 username */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .claim("userId", userId)       // 下游服务据此识别用户身份
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256) // 强制 HS256
                .compact();
    }

    /** 校验并解析 token；若签名错误或已过期，会抛 JwtException */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 token 中取出 username */
    public String getUsername(String token) {

        return parse(token).get("username", String.class);
    }

    /** 从 token 中取出 userId。用 Number 接收再转 Long，避免 jjwt 把数字反序列化成 Integer 时类型不匹配 */
    public Long getUserId(String token) {
        Number n = parse(token).get("userId", Number.class);
        return n == null ? null : n.longValue();
    }

    /** 校验 token 是否有效（签名正确且未过期），供网关过滤器使用 */
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
