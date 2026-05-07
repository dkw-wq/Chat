package com.example.Chat.chat;

import lombok.Data;

@Data
public class ChatMessage {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
}
