package edu.sookmyung.talktitude.config.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // HandShakeInterceptor에서 넣어준 loginId 사용
        String loginId = (String) attributes.get("loginId");
        return () -> loginId; // Principal::getName
    }
}
