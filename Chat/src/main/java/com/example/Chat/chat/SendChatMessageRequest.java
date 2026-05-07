package com.example.Chat.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendChatMessageRequest(
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容不能超过 1000 个字符")
    String content
) {
}
