package com.example.Chat.chat;

import com.example.Chat.user.ChatUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageBroadcaster chatMessageBroadcaster;

    public ChatController(ChatService chatService, ChatMessageBroadcaster chatMessageBroadcaster) {
        this.chatService = chatService;
        this.chatMessageBroadcaster = chatMessageBroadcaster;
    }

    @GetMapping("/{friendId}/messages")
    public List<ChatMessageResponse> messages(
        @PathVariable Long friendId,
        @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        return chatService.listMessages(currentUser.user().getId(), friendId, limit);
    }

    @PostMapping("/{friendId}/messages")
    public ChatMessageResponse sendMessage(
        @PathVariable Long friendId,
        @Valid @RequestBody SendChatMessageRequest request,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        ChatMessageResponse message = chatService.sendMessage(currentUser.user().getId(), friendId, request);
        chatMessageBroadcaster.broadcast(message);
        return message;
    }
}
