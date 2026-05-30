package com.fintech.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.fintech.config.AgentPrompts;
import com.fintech.tool.KnowledgeTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FaqAgentNode implements NodeAction {

    private final ChatClient chatClient;
    private final KnowledgeTools knowledgeTools;

    @Override
    public Map<String, Object> apply(OverAllState t) throws Exception {
        String userMsg = (String) t.value(ChatState.USER_MESSAGE).orElse("");
        String reply = chatClient.prompt()
                .system(AgentPrompts.FaqAgent_Prompt)
                .messages(ChatState.historyMessages(t))
                .user(userMsg)
                .tools(knowledgeTools)
                .call()
                .content();
        return Map.of(
                ChatState.EXPERT_RESPONSE, reply,
                ChatState.FINAL_ANSWER, reply
        );
    }
}
