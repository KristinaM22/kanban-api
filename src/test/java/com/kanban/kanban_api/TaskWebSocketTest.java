package com.kanban.kanban_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanban.kanban_api.model.Priority;
import com.kanban.kanban_api.model.Status;
import com.kanban.kanban_api.dto.TaskEvent;
import com.kanban.kanban_api.dto.TaskInput;
import com.kanban.kanban_api.security.JwtUtil;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
class TaskWebSocketTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JwtUtil jwtUtil;

    private WebSocketStompClient stompClient;
    private String token;

    @BeforeEach
    void setup() {
        token = jwtUtil.generateToken("admin");

        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void whenTaskCreated_thenBroadcastToSubscribers() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<TaskEvent> receivedEvent = new AtomicReference<>();

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", "Bearer " + token);

        StompSession session = stompClient
            .connectAsync("http://localhost:" + port + "/ws", headers, new StompSessionHandlerAdapter() {})
            .get(3, TimeUnit.SECONDS);

        session.subscribe("/topic/tasks", new StompFrameHandler() {
            @Override
            public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                return TaskEvent.class;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                receivedEvent.set((TaskEvent) payload);
                latch.countDown();
            }
        });

        TaskInput input = new TaskInput();
        input.setTitle("Broadcast Test");
        input.setStatus(Status.TO_DO);
        input.setPriority(Priority.LOW);

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(input)))
            .andExpect(status().isCreated());

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Did not receive broadcast");
        TaskEvent event = receivedEvent.get();
        assertEquals("CREATED", event.action());
        assertEquals("Broadcast Test", event.task().getTitle());
    }
}


