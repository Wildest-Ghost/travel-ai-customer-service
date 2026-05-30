package com.fintech.history;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fintech.history.entity.ChatMessage;
import com.fintech.history.entity.ChatSession;
import com.fintech.history.mapper.ChatMessageMapper;
import com.fintech.history.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史持久化（冷数据，存 PostgreSQL，永久保存，给前端会话列表用）。
 *
 * 和 Redis(ConversationMemory) 的分工：
 *  - Redis：热数据，最近 10 条 + 30 分钟 TTL，喂大模型做多轮上下文
 *  - 这里：冷数据，完整对话永久存，供 ChatGPT 式历史列表展示
 *
 * 所有写操作 try-catch 兜底：历史持久化失败不能拖垮主对话（和 Redis 一样的容错原则）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    /** 保存一轮对话：首次出现的 sessionId 自动建会话（标题取第一句话），然后插入两条消息 */
    public void saveTurn(String sessionId, String userId, String userMsg, String botMsg) {
        if (sessionId == null || sessionId.isBlank()) return;
        try {
            ChatSession session = sessionMapper.selectOne(
                    new LambdaQueryWrapper<ChatSession>().eq(ChatSession::getSessionId, sessionId)
            );
            LocalDateTime now = LocalDateTime.now();
            if (session == null) {
                // 首轮 → 新建会话，标题 = 第一句话（截断）
                session = new ChatSession();
                session.setSessionId(sessionId);
                session.setUserId(userId);
                session.setTitle(buildTitle(userMsg));
                session.setCreatedAt(now);
                session.setUpdatedAt(now);
                sessionMapper.insert(session);
            } else {
                // 已存在 → 刷新 updatedAt，让它排到列表最前
                session.setUpdatedAt(now);
                sessionMapper.updateById(session);
            }
            messageMapper.insert(buildMsg(sessionId, "user", userMsg));
            messageMapper.insert(buildMsg(sessionId, "assistant", botMsg));
        } catch (Exception e) {
            log.warn("持久化对话历史失败（已忽略）sessionId={}, 原因={}", sessionId, e.getMessage());
        }
    }

    /** 列出某用户的所有会话，按最近更新倒序（ChatGPT 侧边栏） */
    public List<ChatSession> listSessions(String userId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<ChatSession>()
                .orderByDesc(ChatSession::getUpdatedAt);
        if (userId != null && !userId.isBlank()) {
            wrapper.eq(ChatSession::getUserId, userId);
        }
        return sessionMapper.selectList(wrapper);
    }

    /** 取某会话的全部消息，按时间正序 */
    public List<ChatMessage> listMessages(String sessionId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreatedAt)
                        .orderByAsc(ChatMessage::getId)
        );
    }

    private String buildTitle(String firstMsg) {
        if (firstMsg == null) return "新对话";
        String t = firstMsg.strip();
        return t.length() > 20 ? t.substring(0, 20) + "…" : t;
    }

    private ChatMessage buildMsg(String sessionId, String role, String content) {
        ChatMessage m = new ChatMessage();
        m.setSessionId(sessionId);
        m.setRole(role);
        m.setContent(content);
        m.setCreatedAt(LocalDateTime.now());
        return m;
    }
}
