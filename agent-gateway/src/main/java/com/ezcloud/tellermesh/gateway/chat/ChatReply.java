package com.ezcloud.tellermesh.gateway.chat;

/**
 * Response of {@code POST /chat}. Named ChatReply rather than ChatResponse to avoid clashing
 * with Spring AI's {@code org.springframework.ai.chat.model.ChatResponse}.
 */
public record ChatReply(String conversationId, String reply) {
}
