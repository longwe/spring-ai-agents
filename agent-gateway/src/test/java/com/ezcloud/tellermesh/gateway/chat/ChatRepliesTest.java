package com.ezcloud.tellermesh.gateway.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

class ChatRepliesTest {

    @Test
    void skipsAnEmptyFirstGeneration() {
        // The shape Anthropic returned after a tool call in step 3: "" then the real answer.
        var response = response("", "You spent 2,143.84 USD.");

        assertThat(ChatReplies.text(response)).isEqualTo("You spent 2,143.84 USD.");
    }

    @Test
    void joinsSeveralNonBlankGenerations() {
        var response = response("First part.", "  ", "Second part.");

        assertThat(ChatReplies.text(response)).isEqualTo("First part.\n\nSecond part.");
    }

    @Test
    void nullResponseGivesEmptyText() {
        assertThat(ChatReplies.text(null)).isEmpty();
    }

    private static ChatResponse response(String... texts) {
        var generations = Arrays.stream(texts)
                .map(text -> new Generation(new AssistantMessage(text)))
                .toList();
        return new ChatResponse(generations);
    }
}
