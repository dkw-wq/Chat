package com.example.Chat.impression;

import com.example.Chat.user.ChatUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/impressions")
public class FriendImpressionController {

    private final FriendImpressionService friendImpressionService;

    public FriendImpressionController(FriendImpressionService friendImpressionService) {
        this.friendImpressionService = friendImpressionService;
    }

    @GetMapping("/received")
    public List<FriendImpressionResponse> received(@AuthenticationPrincipal ChatUserDetails currentUser) {
        return friendImpressionService.listReceived(currentUser.user().getId());
    }

    @GetMapping("/given")
    public List<FriendImpressionResponse> given(@AuthenticationPrincipal ChatUserDetails currentUser) {
        return friendImpressionService.listGiven(currentUser.user().getId());
    }

    @PostMapping
    public FriendImpressionResponse add(
        @Valid @RequestBody AddImpressionRequest request,
        @AuthenticationPrincipal ChatUserDetails currentUser
    ) {
        return friendImpressionService.add(currentUser.user().getId(), request);
    }
}
