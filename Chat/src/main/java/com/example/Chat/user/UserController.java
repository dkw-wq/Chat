package com.example.Chat.user;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/mini-profile")
    public MiniProfileResponse miniProfile(
        @PathVariable Long userId,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        return userService.miniProfile(currentUser.user().getId(), userId);
    }

    @PostMapping("/me/avatar")
    public UserProfileResponse uploadAvatar(
        @RequestParam("avatar") @NotNull MultipartFile avatar,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        return userService.uploadAvatar(currentUser.user().getId(), avatar);
    }
}
