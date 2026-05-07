package com.example.Chat.friend;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendFriendRequest(
    @NotNull(message = "请选择要添加的用户")
    Long receiverId,

    @Size(max = 255, message = "验证消息不能超过 255 个字符")
    String message
) {
}
