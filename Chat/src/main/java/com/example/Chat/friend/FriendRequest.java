package com.example.Chat.friend;

import lombok.Data;

@Data
public class FriendRequest {

    private Long id;
    private Long requesterId;
    private Long receiverId;
    private String message;
}
