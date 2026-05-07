package com.example.Chat.user;

public record UserProfileResponse(
    Long id,
    String username,
    String nickname,
    String avatarUrl
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getAvatarUrl()
        );
    }
}
