package edu.sookmyung.talktitude.chat.controller;

import edu.sookmyung.talktitude.chat.dto.ChatMessageRequest;
import edu.sookmyung.talktitude.chat.dto.ChatMessageResponse;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 클라이언트가 "app/chat/send"로 메시지를 보내면 호출됨
    @MessageMapping("chat/send")
    public void handleChatMessage(ChatMessageRequest request) {
        // 1. 메시지 저장 및 변환 처리
        ChatMessage message = chatService.sendMessage(
                request.getSessionId(),
                request.getSenderType(),
                request.getOriginalText(),
                "변환된 응답" // Todo 변환 로직 추가
        );

        // 2. 응답 메시지 DTO 생성
        ChatMessageResponse response = new ChatMessageResponse(
                message.getId(),
                message.getOriginalText(),
                message.getConvertedText(),
                message.getSenderType().name(),
                message.getCreatedAt()
        );

        // 3. 구독자에게 메시지 브로드캐스트 (/topic/session/{sessionId})
        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.getSessionId(),
                response
        );

    }

}
