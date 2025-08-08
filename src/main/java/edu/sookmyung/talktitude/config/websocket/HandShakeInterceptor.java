package edu.sookmyung.talktitude.config.websocket;

import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HandShakeInterceptor implements HandshakeInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String auth  = request.getHeaders().getFirst("Authorization");
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;

        if (token != null && tokenProvider.validToken(token)) {
            attributes.put("userId",   tokenProvider.getUserId(token));     // Long
            attributes.put("userType", tokenProvider.getUserType(token));   // "Member"/"Client"
            attributes.put("loginId",  tokenProvider.getLoginId(token));    // String (Principal용)
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 구현만 해주면 됨 (내용 없어도 OK)
    }
}
