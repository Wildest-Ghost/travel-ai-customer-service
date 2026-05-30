package com.fintech.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 多轮对话记忆，基于 Redis。
 *
 * - key:   chat:history:{sessionId}
 * - value: JSON 数组，每项 {role: "user"/"assistant", content: "..."}
 * - 只保留最近 MAX_MESSAGES 条，超出从头裁掉
 * - 每次写入刷新 30 分钟 TTL（滑动过期）
 *
 * 没有 sessionId 时直接返回空 / 不写入 —— 退化成单轮无记忆，不影响主流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationMemory {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;   // Spring Boot 自动配置的 Jackson

    /** 最多保留最近 10 条消息（≈5 轮问答） */
    private static final int MAX_MESSAGES = 10;
    /** 30 分钟不活跃自动过期 */
    private static final Duration TTL = Duration.ofMinutes(30);
    private static final String KEY_PREFIX = "chat:history:";

    /**
     * 读历史；无 sessionId / 无数据 / Redis 不可用都返回空列表。
     * 容错降级：Redis 故障时退化为单轮无记忆，不影响核心对话。
     */
    public List<Map<String, String>> load(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return new ArrayList<>();
        try {
            String json = redis.opsForValue().get(KEY_PREFIX + sessionId);
            if (json == null || json.isBlank()) return new ArrayList<>();
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            log.warn("读取对话历史失败（降级为无记忆）sessionId={}, 原因={}", sessionId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /** 追加一轮（用户提问 + 助手回复），裁剪后写回并刷新 TTL；Redis 不可用时静默跳过 */
    public void append(String sessionId, String userMsg, String botMsg) {
        if (sessionId == null || sessionId.isBlank()) return;
        try {
            List<Map<String, String>> history = load(sessionId);
            history.add(msg("user", userMsg));
            history.add(msg("assistant", botMsg));
            if (history.size() > MAX_MESSAGES) {
                history = new ArrayList<>(history.subList(history.size() - MAX_MESSAGES, history.size()));
            }
            String json = objectMapper.writeValueAsString(history);
            redis.opsForValue().set(KEY_PREFIX + sessionId, json, TTL);
        } catch (Exception e) {
            log.warn("写入对话历史失败（已忽略）sessionId={}, 原因={}", sessionId, e.getMessage());
        }
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }
}
