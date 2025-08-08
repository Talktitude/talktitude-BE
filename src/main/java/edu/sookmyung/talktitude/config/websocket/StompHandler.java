package edu.sookmyung.talktitude.config.websocket;

import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final ChatSessionRepository chatSessionRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        Map<String, Object> attrs = acc.getSessionAttributes();
        Long userId = (attrs != null) ? (Long) attrs.get("userId") : null;
        String userType = (attrs != null) ? (String) attrs.get("userType") : null;

        if (acc.getCommand() == StompCommand.CONNECT) {
            if (userId == null) throw new AccessDeniedException("Unauthorized");
        }

        if (acc.getCommand() == StompCommand.SUBSCRIBE || acc.getCommand() == StompCommand.SEND) {
            String dest = acc.getDestination(); // /topic/chat/{id} or /user/queue/chat/{id}
            Long sessionId = extractSessionId(dest);

            if (sessionId != null) {
                boolean allowed = chatSessionRepository.findById(sessionId).map(s -> {
                    if ("Member".equalsIgnoreCase(userType)) return s.getMember().getId().equals(userId);
                    if ("Client".equalsIgnoreCase(userType)) return s.getClient().getId().equals(userId);
                    return false;
                }).orElse(false);

                if (!allowed) throw new AccessDeniedException("No permission for session " + sessionId);
            }
        }
        return message;
    }

    private Long extractSessionId(String dest) {
        if (dest == null) return null;
        if (dest.startsWith("/topic/chat/")) return Long.valueOf(dest.substring("/topic/chat/".length()));
        if (dest.startsWith("/user/queue/chat/")) return Long.valueOf(dest.substring("/user/queue/chat/".length()));
        return null;
    }
}