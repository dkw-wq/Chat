package com.example.Chat.impression;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddImpressionRequest(
    @NotNull(message = "请选择好友")
    Long targetId,

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 255, message = "评价内容不能超过 255 个字符")
    String content
) {
}
