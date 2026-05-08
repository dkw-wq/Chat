package com.example.Chat.chat;

public record WebSocketSendMessageRequest(
    String type,
    Long receiverId,
    String content
) {
}
