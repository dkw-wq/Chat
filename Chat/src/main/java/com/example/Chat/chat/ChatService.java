package com.example.Chat.chat;

import com.example.Chat.common.BusinessException;
import com.example.Chat.friend.FriendMapper;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ChatMapper chatMapper;
    private final FriendMapper friendMapper;

    public ChatService(ChatMapper chatMapper, FriendMapper friendMapper) {
        this.chatMapper = chatMapper;
        this.friendMapper = friendMapper;
    }

    public List<ChatMessageResponse> listMessages(Long currentUserId, Long friendId, int limit) {
        requireFriendship(currentUserId, friendId);
        return chatMapper.listConversation(currentUserId, friendId, limit);
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long currentUserId, Long friendId, SendChatMessageRequest request) {
        requireFriendship(currentUserId, friendId);

        ChatMessage message = new ChatMessage();
        message.setSenderId(currentUserId);
        message.setReceiverId(friendId);
        message.setContent(request.content().trim());
        chatMapper.insert(message);

        return chatMapper.findById(message.getId());
    }

    private void requireFriendship(Long currentUserId, Long friendId) {
        if (currentUserId.equals(friendId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不能和自己聊天");
        }
        if (friendMapper.countFriendship(currentUserId, friendId) == 0) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "只能和好友聊天");
        }
    }
}
