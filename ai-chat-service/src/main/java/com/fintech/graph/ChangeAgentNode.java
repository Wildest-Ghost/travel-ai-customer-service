package com.fintech.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.fintech.config.AgentPrompts;
import com.fintech.tool.ChangeTools;
import com.fintech.tool.OrderTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChangeAgentNode implements NodeAction {
    private final ChatClient chatClient;
    private final ChangeTools changeTools;
    private final OrderTools orderTools;

    @Override
    public Map<String, Object> apply(OverAllState t) throws Exception {
        String userMsg = (String) t.value(ChatState.USER_MESSAGE).orElse("");
        String reply = chatClient.prompt()
                .system(AgentPrompts.ChangeAgent_Prompt)
                .messages(ChatState.historyMessages(t))
                .user(userMsg)
                .tools(changeTools,orderTools)
                .call()
                .content();
        return Map.of(
                ChatState.EXPERT_RESPONSE,reply,
                ChatState.FINAL_ANSWER,reply
        );
    }
}
