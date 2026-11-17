package com.ezcloud.tellermesh.gateway.chat;

/**
 * Body of {@code POST /chat}. Leave {@code conversationId} out (or blank) to start a new
 * conversation; send back the ID from a previous {@link ChatReply} to continue one.
 */
public record ChatRequest(String conversationId, String message) {
}
