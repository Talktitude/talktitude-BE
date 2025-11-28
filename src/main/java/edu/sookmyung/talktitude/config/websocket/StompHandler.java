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

        // 허용 접두사 모두 지원: 구독(/user/queue), 브로커(/topic), 송신(/app)까지
        String[] prefixes = { "/user/queue/chat/", "/topic/chat/", "/app/chat/" };

        for (String p : prefixes) {
            int pos = dest.indexOf(p);
            if (pos >= 0) {
                int start = pos + p.length();
                int end = dest.indexOf('/', start); // 다음 슬래시 위치(없으면 끝까지)
                String idStr = (end == -1) ? dest.substring(start) : dest.substring(start, end);
                try {
                    return Long.parseLong(idStr);
                } catch (NumberFormatException ignored) {
                    // 못 파싱하면 다음 접두사 시도
                }
            }
        }
        return null; // 매칭되는 접두사가 없거나 숫자 파싱 실패
    }
}