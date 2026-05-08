package com.example.Chat.chat;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

@Component
public class ChatMessageBroadcaster {

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();

    public ChatMessageBroadcaster(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(Long userId, WebSocketSession session) {
        sessionsByUserId.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByUserId.remove(userId);
        }
    }

    public void broadcast(ChatMessageResponse message) {
        WebSocketEnvelope envelope = new WebSocketEnvelope("CHAT_MESSAGE", message);
        sendToUser(message.senderId(), envelope);
        if (!message.senderId().equals(message.receiverId())) {
            sendToUser(message.receiverId(), envelope);
        }
    }

    public void sendError(WebSocketSession session, String message) {
        send(session, new WebSocketEnvelope("ERROR", new ErrorPayload(message)));
    }

    private void sendToUser(Long userId, WebSocketEnvelope envelope) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null) {
            return;
        }

        sessions.removeIf(session -> !session.isOpen());
        for (WebSocketSession session : sessions) {
            send(session, envelope);
        }
    }

    private void send(WebSocketSession session, WebSocketEnvelope envelope) {
        if (!session.isOpen()) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(envelope);
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                }
            }
        } catch (IOException exception) {
            try {
                session.close();
            } catch (IOException ignored) {
            }
        }
    }

    private record ErrorPayload(String message) {
    }
}
