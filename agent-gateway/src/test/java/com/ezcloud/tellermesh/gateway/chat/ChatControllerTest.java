package com.ezcloud.tellermesh.gateway.chat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Web-layer test: the real controller and validation, with the ChatClient (and so the model) mocked. */
@WebMvcTest(ChatController.class)
class ChatControllerTest {

    private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ChatClient chatClient;

    // One mock per step of the fluent prompt().user(...).advisors(...).call().chatResponse() chain.
    private final ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    private final ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

    @BeforeEach
    @SuppressWarnings("unchecked")
    void modelReplies() {
        // An empty first generation, as Anthropic returns after a tool call, to prove ChatReplies is used.
        var response = new ChatResponse(List.of(
                new Generation(new AssistantMessage("")),
                new Generation(new AssistantMessage("The available balance is 4,015.37 USD."))));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callSpec);
        when(callSpec.chatResponse()).thenReturn(response);
    }

    @Test
    void newConversationGetsAGeneratedIdAndTheJoinedReply() throws Exception {
        mvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "What is the balance of acc-1001-chk?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId", matchesPattern(UUID_PATTERN)))
                .andExpect(jsonPath("$.reply", is("The available balance is 4,015.37 USD.")));
    }

    @Test
    void existingConversationIdIsEchoedAndPassedToMemory() throws Exception {
        mvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"conversationId": "conv-42", "message": "And on savings?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId", is("conv-42")));

        assertConversationIdSentToMemory("conv-42");
    }

    @Test
    void blankConversationIdStartsANewConversation() throws Exception {
        mvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"conversationId": "  ", "message": "Hi"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId", matchesPattern(UUID_PATTERN)));
    }

    @Test
    void blankMessageIs400AndNeverReachesTheModel() throws Exception {
        mvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": "   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));

        verifyNoInteractions(chatClient);
    }

    @Test
    void missingMessageIs400() throws Exception {
        mvc.perform(post("/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"conversationId": "conv-42"}
                                """))
                .andExpect(status().isBadRequest());
    }

    /** Runs the advisor customizer the controller passed, against a mock spec, and checks the param. */
    @SuppressWarnings("unchecked")
    private void assertConversationIdSentToMemory(String expectedId) {
        var captor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestSpec).advisors(captor.capture());
        var spec = mock(ChatClient.AdvisorSpec.class);
        captor.getValue().accept(spec);
        verify(spec).param(ChatMemory.CONVERSATION_ID, expectedId);
    }
}
