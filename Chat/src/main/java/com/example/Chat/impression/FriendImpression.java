package com.example.Chat.impression;

import lombok.Data;

@Data
public class FriendImpression {

    private Long id;
    private Long authorId;
    private Long targetId;
    private String content;
}
