package com.fintech.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "message 不能为空")
    private String message;

    /**
     * 会话 ID：同一轮对话用同一个值，服务端用它从 Redis 取多轮历史。
     * 可空 —— 不传则退化成单轮无记忆。前端通常用 localStorage 存一个 UUID。
     */
    private String sessionId;
}
