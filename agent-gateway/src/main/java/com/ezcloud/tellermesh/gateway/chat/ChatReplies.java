package com.ezcloud.tellermesh.gateway.chat;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

/**
 * Reads the reply text out of a {@link ChatResponse}.
 *
 * <p>After a tool call, Anthropic can return the final answer as several content blocks, which
 * Spring AI maps to several generations, and the first one may be empty. {@code .content()} only
 * reads the first generation, so it can return {@code ""} even though the answer is there. This
 * joins the text of every non-blank generation instead.
 */
public final class ChatReplies {

    private ChatReplies() {
    }

    public static String text(ChatResponse response) {
        return Optional.ofNullable(response)
                .map(ChatResponse::getResults)
                .orElse(List.of())
                .stream()
                .map(Generation::getOutput)
                .map(AssistantMessage::getText)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining("\n\n"));
    }
}
