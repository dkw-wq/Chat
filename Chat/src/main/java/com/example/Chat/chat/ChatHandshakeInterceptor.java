package com.example.Chat.chat;

import com.example.Chat.user.ChatUserDetails;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTRIBUTE = "userId";

    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) {
        Long userId = resolveUserId(request);
        if (userId == null) {
            return false;
        }
        attributes.put(USER_ID_ATTRIBUTE, userId);
        return true;
    }

    @Override
    public void afterHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Exception exception
    ) {
    }

    private Long resolveUserId(ServerHttpRequest request) {
        Principal principal = request.getPrincipal();
        Long userId = resolveUserId(principal);
        if (userId != null || !(request instanceof ServletServerHttpRequest servletRequest)) {
            return userId;
        }

        var session = servletRequest.getServletRequest().getSession(false);
        if (session == null) {
            return null;
        }

        Object context = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (context instanceof SecurityContext securityContext) {
            return resolveUserId(securityContext.getAuthentication());
        }
        return null;
    }

    private Long resolveUserId(Principal principal) {
        if (principal instanceof Authentication authentication
            && authentication.getPrincipal() instanceof ChatUserDetails userDetails) {
            return userDetails.user().getId();
        }
        return null;
    }
}
