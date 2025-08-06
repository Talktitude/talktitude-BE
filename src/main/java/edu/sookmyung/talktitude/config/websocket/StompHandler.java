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
        Long memberId = (attrs != null) ? (Long) attrs.get("memberId") : null;

        if (acc.getCommand() == StompCommand.CONNECT) {
            if (memberId == null) throw new AccessDeniedException("Unauthorized");
        }

        if (acc.getCommand() == StompCommand.SUBSCRIBE || acc.getCommand() == StompCommand.SEND) {
            String dest = acc.getDestination(); // 예: /topic/session/123  또는 /app/chat/send
            Long sessionId = extractSessionId(dest); // 직접 파싱 함수 작성

            if (sessionId != null) {
                // 이 멤버가 해당 세션의 상담원/고객인지 확인
                boolean allowed = chatSessionRepository.findById(sessionId)
                        .map(s -> s.getMember().getId().equals(memberId) /* || 고객 매칭 로직 */)
                        .orElse(false);
                if (!allowed) throw new AccessDeniedException("No permission for session " + sessionId);
            }
        }
        return message;
    }

    private Long extractSessionId(String dest) {
        if (dest == null) return null;
        // /topic/session/{id} 또는 /app/chat/send payload 내부로도 가능
        if (dest.startsWith("/topic/session/")) {
            return Long.valueOf(dest.substring("/topic/session/".length()));
        }
        return null;
    }
}