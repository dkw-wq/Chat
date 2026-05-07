package com.example.Chat.friend;

import com.example.Chat.user.ChatUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping("/users/search")
    public List<UserSearchResponse> searchUsers(
        @RequestParam @NotBlank @Size(max = 32) String keyword,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        return friendService.searchUsers(currentUser.user().getId(), keyword);
    }

    @GetMapping("/friends")
    public List<FriendResponse> friends(@AuthenticationPrincipal ChatUserDetails currentUser) {
        return friendService.listFriends(currentUser.user().getId());
    }

    @DeleteMapping("/friends/{friendId}")
    public MessageResponse deleteFriend(
        @PathVariable Long friendId,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        friendService.deleteFriend(currentUser.user().getId(), friendId);
        return new MessageResponse("已删除好友");
    }

    @PostMapping("/friends/requests")
    public FriendRequestResponse sendRequest(
        @Valid @RequestBody SendFriendRequest request,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        return friendService.sendRequest(currentUser.user().getId(), request);
    }

    @GetMapping("/friends/requests/incoming")
    public List<FriendRequestResponse> incomingRequests(@AuthenticationPrincipal ChatUserDetails currentUser) {
        return friendService.listIncomingRequests(currentUser.user().getId());
    }

    @GetMapping("/friends/requests/outgoing")
    public List<FriendRequestResponse> outgoingRequests(@AuthenticationPrincipal ChatUserDetails currentUser) {
        return friendService.listOutgoingRequests(currentUser.user().getId());
    }

    @PostMapping("/friends/requests/{requestId}/accept")
    public MessageResponse acceptRequest(
        @PathVariable Long requestId,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        friendService.acceptRequest(currentUser.user().getId(), requestId);
        return new MessageResponse("已同意好友申请");
    }

    @PostMapping("/friends/requests/{requestId}/reject")
    public MessageResponse rejectRequest(
        @PathVariable Long requestId,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        friendService.rejectRequest(currentUser.user().getId(), requestId);
        return new MessageResponse("已拒绝好友申请");
    }
}
