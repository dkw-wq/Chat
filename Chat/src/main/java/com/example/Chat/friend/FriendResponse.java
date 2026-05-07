package com.example.Chat.friend;

import java.time.LocalDateTime;

public record FriendResponse(
    Long id,
    String username,
    String nickname,
    String avatarUrl,
    LocalDateTime friendSince
) {
}
