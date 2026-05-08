package com.example.Chat.user;

import java.time.LocalDateTime;

public record MiniProfileResponse(
    Long id,
    String username,
    String nickname,
    String avatarUrl,
    LocalDateTime createdAt,
    boolean friend
) {

    public static MiniProfileResponse from(User user, boolean friend) {
        return new MiniProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getAvatarUrl(),
            user.getCreatedAt(),
            friend
        );
    }
}
