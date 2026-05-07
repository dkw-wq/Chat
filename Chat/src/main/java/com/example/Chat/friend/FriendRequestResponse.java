package com.example.Chat.friend;

import java.time.LocalDateTime;

public record FriendRequestResponse(
    Long id,
    Long requesterId,
    String requesterUsername,
    String requesterNickname,
    String requesterAvatarUrl,
    Long receiverId,
    String receiverUsername,
    String receiverNickname,
    String receiverAvatarUrl,
    String status,
    String message,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
