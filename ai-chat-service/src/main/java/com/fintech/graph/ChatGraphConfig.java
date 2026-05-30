package com.fintech.graph;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ChatGraphConfig {

    private final RouterNode routerNode;
    private final OrderAgentNode orderAgentNode;
    private final ChangeAgentNode changeAgentNode;
    private final FallbackAgentNode fallbackAgentNode;

    @Bean
    public CompiledGraph chatGraph(FaqAgentNode faqAgentNode) throws Exception {

        // StateGraph 需要 OverAllStateFactory：每次 invoke 时生成一份新的 OverAllState 实例
        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            ChatState.keyStrategies().forEach(state::registerKeyAndStrategy);
            return state;
        };

        StateGraph stateGraph = new StateGraph("travel-cs", stateFactory)
                .addNode("router",        AsyncNodeAction.node_async(routerNode))
                .addNode("orderAgent",    AsyncNodeAction.node_async(orderAgentNode))
                .addNode("changeAgent",   AsyncNodeAction.node_async(changeAgentNode))
                .addNode("fallbackAgent", AsyncNodeAction.node_async(fallbackAgentNode))
                .addNode("faqAgent", AsyncNodeAction.node_async(faqAgentNode))

                // 入口
                .addEdge(StateGraph.START, "router")

                // Router 条件边：按路由决策分发到对应专家节点
                .addConditionalEdges("router",
                        AsyncEdgeAction.edge_async(state ->
                                (String) state.value(ChatState.ROUTING_DECISION).orElse("FALLBACK")
                        ),
                        Map.of(
                                "ORDER",    "orderAgent",
                                "CHANGE",   "changeAgent",
                                "FAQ",       "faqAgent",
                                "FALLBACK", "fallbackAgent"
                        )
                )

                // 三个专家执行完都直接到 END
                .addEdge("orderAgent",    StateGraph.END)
                .addEdge("changeAgent",   StateGraph.END)
                .addEdge("faqAgent",   StateGraph.END)
                .addEdge("fallbackAgent", StateGraph.END);

        return stateGraph.compile();
    }
}
