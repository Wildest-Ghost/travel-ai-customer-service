package com.fintech.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {
    public final AgentPrompts agentPrompts;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(buildSystemPrompt(agentPrompts.OrderAgent_Prompt))
                .build();
    }

    private String buildSystemPrompt(String prompt) {
        return prompt;
    }
}
