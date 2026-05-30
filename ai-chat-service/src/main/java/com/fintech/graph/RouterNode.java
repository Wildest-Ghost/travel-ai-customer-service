package com.fintech.graph;


import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.fintech.config.AgentPrompts;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RouterNode implements NodeAction {

    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState t) throws Exception {
        String userMsg = (String) t.value(ChatState.USER_MESSAGE).orElse("");
        String raw = chatClient.prompt()
                .system(AgentPrompts.Router_Prompt)
                .messages(ChatState.historyMessages(t))   // 带历史，"那第二个呢"才能正确路由
                .user(userMsg)
                .call()
                .content()
                .trim()
                .toUpperCase();

        String decision;
        if(raw.startsWith("ORDER")){
            decision = "ORDER";
        }else if(raw.startsWith("CHANGE")){
            decision = "CHANGE";
        } else if (raw.startsWith("FAQ")) {
            decision = "FAQ";
        } else {
            decision = "FALLBACK";
        }
        return Map.of(ChatState.ROUTING_DECISION, decision);
    }
}
