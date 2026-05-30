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
public class FallbackAgentNode implements NodeAction {
    private final ChatClient chatClient;

    @Override
    public Map<String, Object> apply(OverAllState t) throws Exception {
        String userMsg = (String) t.value(ChatState.USER_MESSAGE).orElse("");
        String reply = chatClient.prompt()
                .system(AgentPrompts.FallbackAgent_Prompt)
                .messages(ChatState.historyMessages(t))
                .user(userMsg)
                .call()
                .content();

        return Map.of(
                ChatState.EXPERT_RESPONSE, reply,
                ChatState.FINAL_ANSWER, reply
        );
    }
}
