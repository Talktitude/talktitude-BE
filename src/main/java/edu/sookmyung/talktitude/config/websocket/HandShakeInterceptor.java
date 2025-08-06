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
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        var headers = request.getHeaders();
        String auth = headers.getFirst("Authorization"); // "Bearer xxx"
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;

        if (token != null && tokenProvider.validToken(token)) {
            Long memberId = tokenProvider.getMemberId(token);
            attributes.put("memberId", memberId);
            return true;
        }

        return false; // 유효하지 않으면 거절
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 구현만 해주면 됨 (내용 없어도 OK)
    }
}
