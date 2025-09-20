package edu.sookmyung.talktitude.chat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sookmyung.talktitude.chat.dto.ChatMessageRequest;
import edu.sookmyung.talktitude.chat.dto.ChatMessageResponse;
import edu.sookmyung.talktitude.chat.dto.ClientSessionUpdatedPush;
import edu.sookmyung.talktitude.chat.dto.SessionUpdatedPush;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.SenderType;
import edu.sookmyung.talktitude.chat.service.ChatService;
import edu.sookmyung.talktitude.chat.service.PolitenessClassificationService;
import edu.sookmyung.talktitude.chat.service.RecommendService;
import edu.sookmyung.talktitude.common.util.DateTimeUtils;
import edu.sookmyung.talktitude.config.ai.GPTProperties;
import jakarta.annotation.PostConstruct;
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
    private final PolitenessClassificationService politenessClassificationService;


    @PostConstruct
    public void init() {
        //완전히 눈에 띄게 설정
        log.info("\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4DInit ChatWebSocketController");
        log.info("🚀🚀🚀 NEW CODE DEPLOYED! 새 코드 배포됨! 🚀🚀🚀");
        log.info("🔥 ChatWebSocketController 초기화 완료 ");
        log.info("=".repeat(80));
    }
    @MessageMapping("chat/send")
    public void handleChatMessage(ChatMessageRequest request) {

        // 1. 고객 메시지만 공손화 변환
        String convertedText = null;
        if (request.getSenderType() == SenderType.CLIENT) {
            convertedText = processMessagePoliteness(request.getOriginalText(), request.getSessionId());
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
        long createdAtMs = DateTimeUtils.toEpochMillis(message.getCreatedAt());

        // 3. 수신자별 표시 형태 구성
        // 상담원: 공손문(있으면) 표시, 원문보기 버튼 O
        ChatMessageResponse forAgent  = new ChatMessageResponse(message, "MEMBER");

        // 고객: 항상 원문, 원문보기 버튼 X
        ChatMessageResponse forClient = new ChatMessageResponse(message, "CLIENT");

        // 4. 채팅 메시지 푸시(사용자 큐로 전송)
        messagingTemplate.convertAndSendToUser(agentLoginId,  "/queue/chat/" + sessionId, forAgent);
        messagingTemplate.convertAndSendToUser(clientLoginId, "/queue/chat/" + sessionId, forClient);

        // 5. 고객 메시지일 때만 추천 상태 STARTED 푸시 → 추천답변 비동기 생성/푸시 트리거
        if (message.getSenderType() == SenderType.CLIENT) {
            var started = edu.sookmyung.talktitude.chat.dto.recommend.RecommendStatusPush.builder()
                    .messageId(message.getId())
                    .state("STARTED")
                    .timestamp(System.currentTimeMillis())
                    .build();
            messagingTemplate.convertAndSendToUser(
                    agentLoginId,  "/queue/chat/" + sessionId + "/recommendations/status", started
            );

            // 생성 완료/실패 시 DONE/ERROR는 RecommendService.generateAndPush 내부에서 푸시
            recommendService.generateAndPush(message.getId());
        }

        // 6. 상담원 - 상담 목록 업데이트 푸시 (목록 최상단 정렬용)
        var cs = message.getChatSession();
        var listPush = SessionUpdatedPush.builder()
                .sessionId(cs.getId())
                .clientLoginId(cs.getClient().getLoginId())
                .clientPhone(cs.getClient().getPhone())
                .profileImageUrl(cs.getClient().getProfileImageUrl())
                .status(cs.getStatus().name())
                .lastMessageTime(createdAtMs)
                .build();

        // 상담원 상담 목록 업데이트 큐
        messagingTemplate.convertAndSendToUser(
                agentLoginId,
                "/queue/sessions/updated",
                listPush
        );

        // 고객 - 상담 목록 업데이트 푸시
        ClientSessionUpdatedPush clientListPush = chatService.buildClientUpdatedPush(cs);
        messagingTemplate.convertAndSendToUser(
                clientLoginId,
                "/queue/client/sessions/updated",
                clientListPush
        );
    }

    //공손화 처리 로직
    private String processMessagePoliteness(String originalText, Long sessionId) {
        try{
            log.info("공손 판별 시작");

            PolitenessClassificationService.FilteredMultiHeadResult classificationResult =
                    politenessClassificationService.classify(originalText);

            log.info("classification result: {}", classificationResult);

            String currentText = originalText;
            // 비공손한 경우 1차 변환
            if (classificationResult.isImpolite()) {
                log.info("비공손 메시지로 판별 - 1차 공손화 변환 수행");
                currentText = convertToPolite(currentText, sessionId);

                if (currentText == null) {
                    currentText = originalText; // 변환 실패시 원문 사용
                }

                log.info("1차 변환 결과: {}", currentText);

                return currentText;
            }

            // 2단계: 변환된 텍스트(또는 원문)를 다시 분류하여 부정적 감정 체크
            PolitenessClassificationService.FilteredMultiHeadResult secondResult =
                    politenessClassificationService.classify(currentText);

            log.info("2차 분류 결과: {}", secondResult);
            log.info("secondResult.text: {}", secondResult.text);
            log.info("secondResult.finalJudgment: {}", secondResult.finalJudgment);
            log.info("secondResult.reason: {}", secondResult.reason);
            log.info("secondResult.emotions size: {}", secondResult.emotions.size());

            log.info("finalJudgment: '{}' (길이: {}), !\"polite\".equals(): {}",
                    secondResult.finalJudgment,
                    secondResult.finalJudgment != null ? secondResult.finalJudgment.length() : "null",
                    !"polite".equals(secondResult.finalJudgment));



            // 공손하지만 부정적 감정이 있는 경우 2차 변환
            if (!"polite".equals(secondResult.finalJudgment) && secondResult.hasNegativeEmotions()) {
                log.info("공손하지만 부정적 감정 감지 - 2차 공손화 변환 수행");
                String finalText = convertToPolite(currentText, sessionId);
                if (finalText != null) {
                    currentText = finalText;
                }
                log.info("2차 변환 결과: {}", currentText);
            }
            // 원문과 같으면 null 반환 (변환 없음을 의미)
            return currentText.equals(originalText) ? null : currentText;

        } catch (Exception e) {
            log.error("공손화 처리 중 오류 발생: {}", e.getMessage(), e);
            return null; // 오류 발생 시 원문 사용
        }
    }


    //공손 변환 로직
    public String convertToPolite(String originalMessage, Long sessionId){
        try{
            List<ChatMessage> recentMessages = chatService.getRecentMessages(sessionId,5);
            String context = buildContextString(recentMessages);

            String result = generatePoliteMessage(originalMessage,context);
            JsonNode jsonNode = objectMapper.readTree(result);

            return jsonNode.path("message").asText();

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
