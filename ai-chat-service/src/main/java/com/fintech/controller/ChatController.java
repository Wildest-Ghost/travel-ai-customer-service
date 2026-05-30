package com.fintech.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fintech.common.Result;
import com.fintech.dto.ChatRequest;
import com.fintech.graph.ChatState;
import com.fintech.history.ChatHistoryService;
import com.fintech.history.entity.ChatMessage;
import com.fintech.history.entity.ChatSession;
import com.fintech.memory.ConversationMemory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final CompiledGraph chatGraph;
    private final ConversationMemory memory;          // 热数据：LLM 短期上下文（Redis）
    private final ChatHistoryService historyService;  // 冷数据：永久历史（PostgreSQL）

    @SentinelResource(
            value = "aiChat",
            fallback = "chatFallback",   //业务异常被降级
            blockHandler = "chatBlock"   //被限流/被熔断
    )
    @PostMapping
    public Result<String> chat(@Valid @RequestBody ChatRequest req,
                               @RequestHeader(value = "X-User-Id", required = false) String userId) throws Exception {
        String sessionId = req.getSessionId();

        // 1. Redis 取最近 10 条做 LLM 上下文
        List<Map<String, String>> history = memory.load(sessionId);

        // 2. 构造初始状态
        Map<String, Object> input = new HashMap<>();
        input.put(ChatState.USER_MESSAGE, req.getMessage());
        input.put(ChatState.SESSION_ID, sessionId == null ? "" : sessionId);
        input.put(ChatState.HISTORY, history);
        input.put(ChatState.USER_ID, userId == null ? "" : userId);

        // 3. 跑多智能体 graph
        OverAllState finalState = chatGraph.invoke(input).orElseThrow();
        String answer = (String) finalState.value(ChatState.FINAL_ANSWER).orElse("（无回复）");

        // 4. 双写：Redis（热，喂大模型）+ PostgreSQL（冷，永久历史）
        memory.append(sessionId, req.getMessage(), answer);
        historyService.saveTurn(sessionId, userId, req.getMessage(), answer);

        return Result.success(answer);
    }

    /** 会话列表（ChatGPT 侧边栏）：当前用户的所有会话，按最近更新倒序 */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> sessions(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return Result.success(historyService.listSessions(userId));
    }

    /** 某会话的全部消息（点击侧边栏某条会话时加载） */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessage>> messages(@PathVariable String sessionId) {
        return Result.success(historyService.listMessages(sessionId));
    }

    // fallback：签名 = 原方法参数 + Throwable
    public Result<String> chatFallback(ChatRequest req, String userId, Throwable ex) {
        log.error("AI 调用失败（被降级），真实异常：", ex);
        return Result.success("客服繁忙，请稍后再试～");
    }

    // blockHandler：签名 = 原方法参数 + BlockException
    public Result<String> chatBlock(ChatRequest req, String userId, BlockException ex) {
        return Result.success("当前咨询人数过多，请稍后再试～");
    }
}
