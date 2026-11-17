package com.ezcloud.tellermesh.gateway.chat;

import jakarta.validation.constraints.NotBlank;

/**
 * Body of {@code POST /chat}. Leave {@code conversationId} out (or blank) to start a new
 * conversation; send back the ID from a previous {@link ChatReply} to continue one.
 * {@code message} is required; a missing or blank one is rejected with 400.
 */
public record ChatRequest(String conversationId, @NotBlank String message) {
}
