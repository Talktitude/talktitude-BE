package edu.sookmyung.talktitude.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final HandShakeInterceptor handShakeInterceptor;
    private final StompHandler stompHandler;
    private final CustomHandshakeHandler customHandshakeHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 endpoint
        registry.addEndpoint("/ws")
                .addInterceptors(handShakeInterceptor)
                .setHandshakeHandler(customHandshakeHandler) // Principal 주입
                .setAllowedOriginPatterns(
                        "https://localhost:3000",
                        "https://localhost:3001",
                        "https://talktitude-client-fe.vercel.app"
                )
                .withSockJS();
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // 구독 주소 (메시지를 구독(수신)하는 요청 엔드포인트)
        registry.setApplicationDestinationPrefixes("/app"); // 발신 주소 (메시지를 발행(송신)하는 엔드포인트)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration
                .taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(32)
                .queueCapacity(1000)
                .keepAliveSeconds(60);
        registration.interceptors(stompHandler);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration
                .taskExecutor()
                .corePoolSize(8)
                .maxPoolSize(32)
                .queueCapacity(1000)
                .keepAliveSeconds(60);
    }

}
