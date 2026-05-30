package com.fintech.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.fintech.config.AgentPrompts;
import com.fintech.tool.OrderTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderAgentNode implements NodeAction {
    private final ChatClient chatClient;
    private final OrderTools orderTools;

    @Override
    public Map<String, Object> apply(OverAllState t) throws Exception {
        String userMsg=(String) t.value(ChatState.USER_MESSAGE).orElse("");
        String userId=(String) t.value(ChatState.USER_ID).orElse("");

        String sys = AgentPrompts.OrderAgent_Prompt;
        if (userId != null && !userId.isEmpty()) {
            sys = sys + "\n\n【当前登录用户ID】" + userId + "。" +
            "用户说\"我的订单/我所有的订单\"时，用这个 userId 调 findOrdersByUserId\n" +
                    "不要反过来问用户要 ID（他已经登录了）\n" +
                    "不要编造 userId";
        }
        String reply = chatClient.prompt()
                .system(sys)
                .messages(ChatState.historyMessages(t))
                .user(userMsg)
                .tools(orderTools)
                .call()
                .content();
        return Map.of(ChatState.EXPERT_RESPONSE, reply,
                      ChatState.FINAL_ANSWER,reply);
    }
}
