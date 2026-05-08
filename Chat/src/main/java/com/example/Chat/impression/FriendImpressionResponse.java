package com.example.Chat.impression;

import java.time.LocalDateTime;

public record FriendImpressionResponse(
    Long id,
    Long authorId,
    String authorUsername,
    String authorNickname,
    Long targetId,
    String targetUsername,
    String targetNickname,
    String content,
    LocalDateTime createdAt
) {
}
