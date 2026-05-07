package com.example.Chat.friend;

public record UserSearchResponse(
    Long id,
    String username,
    String nickname,
    String avatarUrl,
    String relationshipStatus
) {
}
