package edu.sookmyung.talktitude.config.websocket;

import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
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

        // 로그 추가
        log.debug("[STOMP] cmd={} dest={} userId={} userType={}",
                acc.getCommand(), acc.getDestination(), userId, userType);

        // CONNECT 시 필수 값 검증
        if (acc.getCommand() == StompCommand.CONNECT) {
            if (userId == null || userType == null) {
                log.warn("[STOMP] CONNECT rejected. userId={}, userType={}", userId, userType);
                throw new AccessDeniedException("Unauthorized STOMP CONNECT");
            }
        }

        // SUBSCRIBE / SEND 검증
        if (acc.getCommand() == StompCommand.SUBSCRIBE || acc.getCommand() == StompCommand.SEND) {
            String dest = acc.getDestination();
            Long sessionId = extractSessionId(dest);

            if (sessionId != null) {
                boolean allowed = chatSessionRepository.findById(sessionId).map(s -> {
                    if ("Member".equalsIgnoreCase(userType)) return s.getMember().getId().equals(userId);
                    if ("Client".equalsIgnoreCase(userType)) return s.getClient().getId().equals(userId);
                    return false;
                }).orElse(false);

                if (!allowed) {
                    log.warn("[STOMP] Access denied. userId={} userType={} dest={} sessionId={}",
                            userId, userType, dest, sessionId);
                    throw new AccessDeniedException("No permission for chat session " + sessionId);
                } else {
                    log.debug("[STOMP] Access granted. userId={} dest={}", userId, dest);
                }
            }
        }

        return message;
    }

    private Long extractSessionId(String dest) {
        if (dest == null) return null;
        try {
            if (dest.startsWith("/topic/chat/")) {
                return Long.valueOf(dest.substring("/topic/chat/".length()));
            }
            if (dest.startsWith("/user/queue/chat/")) {
                return Long.valueOf(dest.substring("/user/queue/chat/".length()));
            }
        } catch (NumberFormatException e) {
            log.error("[STOMP] Invalid sessionId in destination: {}", dest, e);
        }
        return null;
    }
}