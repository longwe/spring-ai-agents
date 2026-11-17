package com.ezcloud.tellermesh.gateway.config;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.core.Ordered;

/**
 * Appends today's date to the system message on every request, so the model can turn
 * relative periods ("last week", "the last 30 days") into concrete tool arguments.
 * The date is read per request rather than baked into the prompt when the bean is built.
 */
public class CurrentDateAdvisor implements BaseAdvisor {

    private final Clock clock;

    public CurrentDateAdvisor(Clock clock) {
        this.clock = clock;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        var dateLine = "Today's date is %s. Use it to work out date ranges for tools."
                .formatted(LocalDate.now(clock));
        var prompt = request.prompt().augmentSystemMessage(
                system -> new SystemMessage(system.getText() + "\n" + dateLine));
        return request.mutate().prompt(prompt).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
