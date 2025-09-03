package edu.sookmyung.talktitude.chat.controller;

import edu.sookmyung.talktitude.chat.dto.ChatMessageRequest;
import edu.sookmyung.talktitude.chat.dto.ChatMessageResponse;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.SenderType;
import edu.sookmyung.talktitude.chat.service.ChatService;
import edu.sookmyung.talktitude.chat.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RecommendService recommendService;

    @MessageMapping("chat/send")
    public void handleChatMessage(ChatMessageRequest request) {
        ChatMessage message = chatService.sendMessage(
                request.getSessionId(),
                request.getSenderType(),
                request.getOriginalText(),
                "변환된 응답" // TODO: 실제 공손화
        );

        Long sessionId = request.getSessionId();
        String agentLoginId  = message.getChatSession().getMember().getLoginId();
        String clientLoginId = message.getChatSession().getClient().getLoginId();

        // 상담원: 공손문(있으면) 표시, 원문보기 버튼 O
        ChatMessageResponse forAgent = new ChatMessageResponse(
                message.getId(),
                (message.getConvertedText() != null) ? message.getConvertedText() : message.getOriginalText(),
                message.getOriginalText(),
                (message.getConvertedText() != null),
                message.getSenderType().name(),
                message.getCreatedAt()
        );

        // 고객: 항상 원문, 원문보기 버튼 X
        ChatMessageResponse forClient = new ChatMessageResponse(
                message.getId(),
                message.getOriginalText(),
                message.getOriginalText(),
                false,
                message.getSenderType().name(),
                message.getCreatedAt()
        );

        // 👇 사용자 큐로 전송
        messagingTemplate.convertAndSendToUser(agentLoginId,  "/queue/chat/" + sessionId, forAgent);
        messagingTemplate.convertAndSendToUser(clientLoginId, "/queue/chat/" + sessionId, forClient);

        // 메시지 저장 직후 추천답변 비동기 생성 & 푸시
        recommendService.generateAndPush(message.getId());
    }
}
