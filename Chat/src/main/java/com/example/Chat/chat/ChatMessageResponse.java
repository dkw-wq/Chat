package com.example.Chat.chat;

import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long id,
    Long senderId,
    String senderUsername,
    String senderNickname,
    Long receiverId,
    String receiverUsername,
    String receiverNickname,
    String content,
    LocalDateTime createdAt
) {
}
