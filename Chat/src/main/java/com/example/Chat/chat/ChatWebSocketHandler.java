package com.example.Chat.chat;

import com.example.Chat.common.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final ChatMessageBroadcaster chatMessageBroadcaster;

    public ChatWebSocketHandler(
        ObjectMapper objectMapper,
        ChatService chatService,
        ChatMessageBroadcaster chatMessageBroadcaster
    ) {
        this.objectMapper = objectMapper;
        this.chatService = chatService;
        this.chatMessageBroadcaster = chatMessageBroadcaster;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = currentUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("请先登录"));
            return;
        }
        chatMessageBroadcaster.register(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = currentUserId(session);
        if (userId == null) {
            chatMessageBroadcaster.sendError(session, "请先登录");
            return;
        }

        try {
            WebSocketSendMessageRequest request = objectMapper.readValue(
                message.getPayload(),
                WebSocketSendMessageRequest.class
            );
            if (!"SEND_MESSAGE".equals(request.type())) {
                chatMessageBroadcaster.sendError(session, "不支持的消息类型");
                return;
            }

            ChatMessageResponse savedMessage = chatService.sendMessage(
                userId,
                request.receiverId(),
                new SendChatMessageRequest(request.content())
            );
            chatMessageBroadcaster.broadcast(savedMessage);
        } catch (BusinessException exception) {
            chatMessageBroadcaster.sendError(session, exception.getMessage());
        } catch (Exception exception) {
            chatMessageBroadcaster.sendError(session, "消息格式不正确");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = currentUserId(session);
        if (userId != null) {
            chatMessageBroadcaster.unregister(userId, session);
        }
    }

    private Long currentUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get(ChatHandshakeInterceptor.USER_ID_ATTRIBUTE);
        return userId instanceof Long value ? value : null;
    }
}
