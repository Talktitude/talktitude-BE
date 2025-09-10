package edu.sookmyung.talktitude.chat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sookmyung.talktitude.chat.dto.ChatMessageRequest;
import edu.sookmyung.talktitude.chat.dto.ChatMessageResponse;
import edu.sookmyung.talktitude.chat.dto.SessionUpdatedPush;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.SenderType;
import edu.sookmyung.talktitude.chat.service.ChatService;
import edu.sookmyung.talktitude.chat.service.RecommendService;
import edu.sookmyung.talktitude.config.ai.GPTProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RecommendService recommendService;
    private final ChatClient chatClient;
    private final GPTProperties gptProperties;
    private final ObjectMapper objectMapper;

    @MessageMapping("chat/send")
    public void handleChatMessage(ChatMessageRequest request) {

        // 1. 고객 메시지만 공손화 변환
        String convertedText = null;
        if (request.getSenderType() == SenderType.CLIENT) {
            convertedText = convertToPolite(request.getOriginalText(), request.getSessionId());
        }

        // 2. 메시지 저장
        ChatMessage message = chatService.sendMessage(
                request.getSessionId(),
                request.getSenderType(),
                request.getOriginalText(),
                convertedText
        );

        Long sessionId = request.getSessionId();
        String agentLoginId  = message.getChatSession().getMember().getLoginId();
        String clientLoginId = message.getChatSession().getClient().getLoginId();

        // 3. 수신자별 표시 형태 구성
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

        // 4. 채팅 메시지 푸시(사용자 큐로 전송)
        messagingTemplate.convertAndSendToUser(agentLoginId,  "/queue/chat/" + sessionId, forAgent);
        messagingTemplate.convertAndSendToUser(clientLoginId, "/queue/chat/" + sessionId, forClient);

        // 5. 고객 메시지일 때만 추천답변 비동기 생성/푸시
        if (message.getSenderType() == SenderType.CLIENT) {
            recommendService.generateAndPush(message.getId());
        }

        // 6. 상담 목록 업데이트 푸시 (목록 최상단 정렬용)
        var cs = message.getChatSession();
        var listPush = SessionUpdatedPush.builder()
                .sessionId(cs.getId())
                .clientLoginId(cs.getClient().getLoginId())
                .clientPhone(cs.getClient().getPhone())
                .profileImageUrl(cs.getClient().getProfileImageUrl())
                .status(cs.getStatus().name())
                .lastMessageTime(message.getCreatedAt())
                .build();

        // 상담원 상담 목록 업데이트 큐
        messagingTemplate.convertAndSendToUser(
                agentLoginId,
                "/queue/sessions/updated",
                listPush
        );
    }

    //공손 변환 로직
    public String convertToPolite(String originalMessage, Long sessionId){
        try{
            List<ChatMessage> recentMessages = chatService.getRecentMessages(sessionId,5);
            String context = buildContextString(recentMessages);

            String result = generatePoliteMessage(originalMessage,context);
            JsonNode jsonNode = objectMapper.readTree(result);

            String label = jsonNode.path("label").asText();

            if ("impolite".equalsIgnoreCase(label)) {
                // 무례한 메시지인 경우 공손한 버전 반환
                return jsonNode.path("message").asText();
            } else {
                // 이미 공손한 메시지인 경우 원문 그대로 반환 (null이면 원문 사용)
                return null;
            }

        }catch(Exception e){
            log.error("공손화 처리 중 오류 발생: {}", e.getMessage(), e);
            return null; //오류 발생 시 원문 사용
        }
    }

    public String generatePoliteMessage(String originalMessage, String context){
        GPTProperties.PoliteConfig config = gptProperties.getPolite();
        String fullPrompt = config.getPolitePrompt() +
                "\n\n대화 맥락:\n" + context +
                "\n변환할 메시지: " + originalMessage;

        return chatClient.prompt()
                .user(u -> u.text(fullPrompt))
                .options(OpenAiChatOptions.builder()
                        .model(config.getModel())
                        .temperature(config.getTemperature())
                        .maxTokens(config.getMaxTokens())
                        .build())
                .call()
                .content();
    }

    private String buildContextString(List<ChatMessage> recentMessages){
        StringBuilder builder = new StringBuilder();

        for(ChatMessage message : recentMessages){
            builder.append(message.getSenderType()).append(": ").append(message.getOriginalText()).append("\n");
        }

        return builder.toString();
    }
}
