package com.example.Chat.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度应为 3 到 32 个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    String username,

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 72, message = "密码长度应为 6 到 72 个字符")
    String password,

    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称不能超过 30 个字符")
    String nickname
) {
}
