package edu.sookmyung.talktitude.config.websocket;

import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandShakeInterceptor implements HandshakeInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        // 1) 사전 예외: CORS preflight(OPTIONS) / SockJS info 는 통과
        HttpMethod method = request.getMethod();
        if (method == HttpMethod.OPTIONS) {
            if (log.isDebugEnabled()) log.debug("[WS] OPTIONS preflight pass");
            return true;
        }

        String path = "";
        URI uri = request.getURI();
        if (uri != null) path = uri.getPath() == null ? "" : uri.getPath();

        if (path.contains("/ws/info")) {
            if (log.isDebugEnabled()) log.debug("[WS] SockJS /ws/info pass");
            return true;
        }

        // 2) 토큰 추출: Authorization 헤더 → 쿼리 스트링(token=) 순으로 시도
        String authHeader = request.getHeaders().getFirst("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        if ((token == null || token.isBlank()) && uri != null && uri.getQuery() != null) {
            // 예: ws://host/ws/xxx/websocket?token=eyJ...
            for (String kv : uri.getQuery().split("&")) {
                int i = kv.indexOf('=');
                if (i > 0) {
                    String k = kv.substring(0, i);
                    String v = kv.substring(i + 1);
                    if ("token".equalsIgnoreCase(k) && v != null && !v.isBlank()) {
                        token = v;
                        break;
                    }
                }
            }
        }

        if (token != null && tokenProvider.validToken(token)) {
            // 3) 세션 속성 주입 (CustomHandshakeHandler에서 Principal 생성에 사용)
            attributes.put("userId",   tokenProvider.getUserId(token));     // Long
            attributes.put("userType", tokenProvider.getUserType(token));   // "Member"/"Client"
            attributes.put("loginId",  tokenProvider.getLoginId(token));    // String
            if (log.isDebugEnabled()) log.debug("[WS] token ok. loginId={}", attributes.get("loginId"));
            return true;
        }

        // 토큰이 없거나 유효하지 않음 → 거절
        log.warn("[WS] handshake rejected: invalid or missing token. path={}", path);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        if (exception != null) {
            log.warn("[WS] afterHandshake with exception", exception);
        } else if (log.isDebugEnabled()) {
            log.debug("[WS] afterHandshake ok");
        }
    }
}
