package edu.sookmyung.talktitude.config.websocket;

import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
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

        // 1) CORS preflight(OPTIONS) 또는 SockJS info 요청은 무조건 통과
        String path = request.getURI() != null ? request.getURI().getPath() : "";
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return true;
        }
        // SockJS 사전 확인: GET /ws/info (또는 /ws/info?...)
        if (path != null && path.contains("/ws/info")) {
            return true;
        }

        // 2) 실제 핸드셰이크 단계에서만 토큰 검사
        String auth  = request.getHeaders().getFirst("Authorization");
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;

        if (token != null && tokenProvider.validToken(token)) {
            attributes.put("userId",   tokenProvider.getUserId(token));     // Long
            attributes.put("userType", tokenProvider.getUserType(token));   // "Member"/"Client"
            attributes.put("loginId",  tokenProvider.getLoginId(token));    // String (Principal용)
            return true;
        }

        // 토큰 없거나 유효하지 않으면 연결 거절
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
