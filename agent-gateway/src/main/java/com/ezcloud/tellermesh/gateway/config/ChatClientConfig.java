package com.ezcloud.tellermesh.gateway.config;

import java.time.Clock;

import com.ezcloud.tellermesh.gateway.tools.AccountTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    static final String SYSTEM_PROMPT = """
            You are TellerMesh, a retail banking assistant.
            Answer questions about the customer's accounts, transactions and cheque books.
            Be concise. If a request is outside retail banking, say you can't help with it.
            """;

    // ChatMemory is auto-configured by Spring AI: a MessageWindowChatMemory keeping the last
    // 20 messages per conversation, held in memory (lost on restart). Callers pick the
    // conversation per request with advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id)).
    @Bean
    ChatClient chatClient(ChatClient.Builder builder, AccountTools accountTools, ChatMemory chatMemory) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(accountTools)
                .defaultAdvisors(
                        new CurrentDateAdvisor(Clock.systemDefaultZone()),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
