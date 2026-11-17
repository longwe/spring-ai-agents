package com.ezcloud.tellermesh.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

class CurrentDateAdvisorTest {

    // 23:30 UTC on 14 March is already 15 March in Tokyo: the advisor must use the clock's zone.
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-03-14T23:30:00Z"), ZoneId.of("Asia/Tokyo"));

    private final CurrentDateAdvisor advisor = new CurrentDateAdvisor(CLOCK);

    @Test
    void appendsTodaysDateToTheSystemMessage() {
        var request = request(new Prompt(List.of(
                new SystemMessage("You are TellerMesh."),
                new UserMessage("How much did I spend last week?"))));

        var advised = advisor.before(request, mock(AdvisorChain.class));

        var systemText = advised.prompt().getSystemMessage().getText();
        assertThat(systemText)
                .startsWith("You are TellerMesh.")
                .contains("Today's date is 2026-03-15.");
    }

    @Test
    void leavesTheUserMessageUnchanged() {
        var request = request(new Prompt(List.of(
                new SystemMessage("You are TellerMesh."),
                new UserMessage("How much did I spend last week?"))));

        var advised = advisor.before(request, mock(AdvisorChain.class));

        assertThat(advised.prompt().getUserMessage().getText())
                .isEqualTo("How much did I spend last week?");
    }

    @Test
    void addsTheDateEvenWithoutASystemMessage() {
        var request = request(new Prompt(List.of(new UserMessage("Hi"))));

        var advised = advisor.before(request, mock(AdvisorChain.class));

        assertThat(advised.prompt().getSystemMessage().getText())
                .contains("Today's date is 2026-03-15.");
    }

    private static ChatClientRequest request(Prompt prompt) {
        return new ChatClientRequest(prompt, Map.of());
    }
}
