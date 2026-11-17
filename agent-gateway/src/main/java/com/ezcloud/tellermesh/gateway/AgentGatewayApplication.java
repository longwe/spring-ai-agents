package com.ezcloud.tellermesh.gateway;

import com.ezcloud.tellermesh.gateway.chat.ChatReplies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AgentGatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(AgentGatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AgentGatewayApplication.class, args);
    }

    // Temporary smoke check for step 1; remove once POST /chat exists (step 4).
    @Bean
    CommandLineRunner helloModel(ChatClient chatClient) {
        return args -> {
            var reply = chatClient.prompt()
                    //.user("Show me the last 5 transactions on acc-1001-chk?")
                   // .user("How much did I spend on acc-1001-chk in the last 30 days?")
                    .user("Show transactions for acc-9999")
                    .call()
                    .chatResponse();
            log.info("Model reply: {}", ChatReplies.text(reply));
        };
    }
}
