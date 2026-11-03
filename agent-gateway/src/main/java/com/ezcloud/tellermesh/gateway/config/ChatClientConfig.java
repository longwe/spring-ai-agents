package com.ezcloud.tellermesh.gateway.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class    ChatClientConfig {

    static final String SYSTEM_PROMPT = """
            You are TellerMesh, a retail banking assistant.
            Answer questions about the customer's accounts, transactions and cheque books.
            Be concise. If a request is outside retail banking, say you can't help with it.
            """;

    // Tools (step 2-3) and chat memory (step 4) are attached here via defaultTools/defaultAdvisors.
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }
}
