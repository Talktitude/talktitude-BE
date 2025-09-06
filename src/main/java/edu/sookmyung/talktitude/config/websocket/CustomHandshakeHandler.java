package edu.sookmyung.talktitude.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String loginId = (String) attributes.get("loginId");

        if (loginId == null || loginId.isBlank()) {
            loginId = "anon-" + UUID.randomUUID();
            log.warn("[WS] Principal missing. Assigned temp loginId={}", loginId);
        } else {
            log.info("[WS] Principal resolved loginId={}", loginId);
        }

        final String principalName = loginId;
        return () -> principalName;
    }
}
