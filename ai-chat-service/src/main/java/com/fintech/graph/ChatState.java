package com.fintech.graph;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatState {

    public static final String USER_MESSAGE = "userMessage";
    public static final String USER_ID = "userId";
    public static final String ROUTING_DECISION = "routingDecision";
    public static final String EXPERT_RESPONSE = "expertResponse";
    public static final String FINAL_ANSWER = "finalAnswer";
    public static final String SESSION_ID = "sessionId";
    /** 多轮历史：List<Map<String,String>>，每项 {role, content} */
    public static final String HISTORY = "history";

    public static Map<String, KeyStrategy> keyStrategies() {
        Map<String, KeyStrategy> map = new HashMap<>();
        map.put(USER_MESSAGE, new ReplaceStrategy());
        map.put(USER_ID, new ReplaceStrategy());
        map.put(ROUTING_DECISION, new ReplaceStrategy());
        map.put(EXPERT_RESPONSE, new ReplaceStrategy());
        map.put(FINAL_ANSWER, new ReplaceStrategy());
        map.put(SESSION_ID, new ReplaceStrategy());
        map.put(HISTORY, new ReplaceStrategy());
        return map;
    }

    /**
     * 把 state 里的历史（List<Map<String,String>>）转成 Spring AI 的 Message 列表，
     * 供各节点 chatClient.prompt().messages(...) 注入上下文。
     * 历史里 role=assistant 的转成 AssistantMessage，其余当 UserMessage。
     */
    public static List<Message> historyMessages(OverAllState state) {
        Object raw = state.value(HISTORY).orElse(null);
        List<Message> messages = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    String role = String.valueOf(m.get("role"));
                    String content = String.valueOf(m.get("content"));
                    if ("assistant".equals(role)) {
                        messages.add(new AssistantMessage(content));
                    } else {
                        messages.add(new UserMessage(content));
                    }
                }
            }
        }
        return messages;
    }
}
