package com.example.Chat.user;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class User {

    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
