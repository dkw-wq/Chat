package com.example.Chat.chat;

public record WebSocketEnvelope(
    String type,
    Object payload
) {
}
