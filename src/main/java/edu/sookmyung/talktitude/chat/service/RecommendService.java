package edu.sookmyung.talktitude.chat.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sookmyung.talktitude.chat.dto.recommend.LlmSuggestItem;
import edu.sookmyung.talktitude.chat.dto.recommend.RecommendItemDto;
import edu.sookmyung.talktitude.chat.dto.recommend.RecommendListDto;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.model.Recommend;
import edu.sookmyung.talktitude.chat.model.Status;
import edu.sookmyung.talktitude.chat.recommend.kb.KnowledgeBase;
import edu.sookmyung.talktitude.chat.recommend.llm.PromptBuilder;
import edu.sookmyung.talktitude.chat.repository.ChatMessageRepository;
import edu.sookmyung.talktitude.chat.repository.RecommendRepository;
import edu.sookmyung.talktitude.chat.recommend.facts.OrderFactsClient;
import edu.sookmyung.talktitude.chat.recommend.intent.IntentService;
import edu.sookmyung.talktitude.config.ai.GptClient;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.chat.rag.search.DocRetrievalFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ChatMessageRepository messageRepo;
    private final RecommendRepository recommendRepo;
    private final IntentService intentService;
    private final OrderFactsClient factsClient;
    private final GptClient gpt;
    private final PromptBuilder promptBuilder;
    private final SimpMessagingTemplate messaging;
    // PG RAG 경로 통합 파사드 (토글 포함)
    private final DocRetrievalFacade docRetriever;

    private final ObjectMapper om = new ObjectMapper();

    @Value("${recommend.generate.topk:4}") private int topK;
    @Value("${recommend.generate.n:4}") private int N;
    @Value("${recommend.generate.biz-days:3}") private int bizDays;

    @Transactional(readOnly = true)
    public RecommendListDto list(Long messageId) {
        ChatMessage m = messageRepo.findById(messageId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_REQUEST));

        // 종료된 세션이면 기존에 저장된 추천이 있어도 보내지 않음
        if (m.getChatSession().getStatus() == Status.FINISHED) {
            return RecommendListDto.builder().messageId(null).items(List.of()).build();
        }

        List<Recommend> rows = recommendRepo.findByMessage_IdOrderByPriorityAsc(messageId);
        List<RecommendItemDto> items = rows.stream()
                .map(r -> RecommendItemDto.builder()
                        .id(r.getId())
                        .text(r.getResponseText())
                        .priority(r.getPriority())
                        .policyIds(Collections.emptyList())
                        .build())
                .toList();
        return RecommendListDto.builder().messageId(messageId).items(items).build();
    }

    /** 메시지 저장 직후 비동기로 호출해 생성 → 저장 → WebSocket push */
    @Async
    @Transactional
    public void generateAndPush(Long messageId) {
        ChatMessage m = messageRepo.findById(messageId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_REQUEST));
        ChatSession s = m.getChatSession();

        String agentLoginId = s.getMember().getLoginId();
        String dataDest = "/queue/chat/" + s.getId() + "/recommendations";
        String statusDest = "/queue/chat/" + s.getId() + "/recommendations/status";

        try {
            // 세션이 종료된 경우
            if (s.getStatus() == Status.FINISHED) {
                var done = edu.sookmyung.talktitude.chat.dto.recommend.RecommendStatusPush.builder()
                        .messageId(messageId)
                        .state("DONE")
                        .reason("SESSION_FINISHED")
                        .timestamp(System.currentTimeMillis())
                        .build();
                messaging.convertAndSendToUser(agentLoginId, statusDest, done);
                return; // 추천 생성 안 하고 종료
            }

            // 추천 답변 생성 시도
            RecommendListDto dto = generate(messageId);

            // 종료 세션이거나 생성 결과가 비었으면 푸시하지 않음
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                var done= edu.sookmyung.talktitude.chat.dto.recommend.RecommendStatusPush.builder()
                        .messageId(messageId)
                        .state("DONE")
                        .reason("NO_ITEMS")
                        .timestamp(System.currentTimeMillis())
                        .build();
                messaging.convertAndSendToUser(agentLoginId, statusDest, done);
                return; // 여기서 종료
            }

            // 여기까지 오면 정상적으로 추천이 생성됨
            // 1) 데이터 푸시
            messaging.convertAndSendToUser(agentLoginId, dataDest, dto);

            // 2) 상태: DONE
            var done = edu.sookmyung.talktitude.chat.dto.recommend.RecommendStatusPush.builder()
                    .messageId(messageId)
                    .state("DONE")
                    .timestamp(System.currentTimeMillis())
                    .build();
            messaging.convertAndSendToUser(agentLoginId, statusDest, done);

        } catch (Exception e) {
            // 상태: ERROR
            var err = edu.sookmyung.talktitude.chat.dto.recommend.RecommendStatusPush.builder()
                    .messageId(messageId)
                    .state("ERROR")
                    .reason("GENERATION_FAILED")
                    .timestamp(System.currentTimeMillis())
                    .build();
            messaging.convertAndSendToUser(agentLoginId, statusDest, err);
            throw e; // 로깅/모니터링 위해 기존 흐름 유지
        }

    }

    /** 동기 생성(REST로 즉시 요청 시) */
    @Transactional
    public RecommendListDto generate(Long messageId) {
        ChatMessage m = messageRepo.findById(messageId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_REQUEST));

        // 종료된 세션인 경우 빈 값
        if (m.getChatSession().getStatus() == Status.FINISHED) {
            return RecommendListDto.builder().messageId(null).items(List.of()).build();
        }

        // 이미 있으면 바로 반환(중복 생성 방지)
        List<Recommend> existing = recommendRepo.findByMessage_IdOrderByPriorityAsc(messageId);
        if (!existing.isEmpty()) {
            return list(messageId);
        }

        // 1) intent
        String intent = intentService.classify(m.getOriginalText());

        // 2) order facts (떡볶이/금액/시간 등 포함)
        var facts = factsClient.buildFacts(m, m.getChatSession().getOrder());

        // 3) RAG: 토글 포함 파사드 사용 (PG pgvector or 기존 키워드형)
        List<KnowledgeBase.Doc> docs = docRetriever.retrieve(m.getOriginalText(), intent, topK);

        // 4) LLM 호출 페이로드 구성
        JsonNode payload = promptBuilder.build(docs, m.getOriginalText(), intent, facts, N, bizDays);
        JsonNode res = gpt.chat(payload);

        // 5) 파싱 (강화)
        List<LlmSuggestItem> items = parseSuggestions(res);

        // 6) 필터링(안전/길이)
        List<LlmSuggestItem> safe = items.stream()
                .filter(it -> it.getText() != null && !it.getText().isBlank())
                .filter(it -> it.getText().length() <= 400)
                .filter(it -> !"high".equalsIgnoreCase(it.getRisk()))
                .collect(Collectors.toList());

        if (safe.isEmpty()) {
            // 폴백 2~3개 (즉시 반응성 개선)
            safe = List.of(
                    new LlmSuggestItem("불편을 드려 죄송합니다. 주문 내역을 확인 후 가능한 조치를 안내드리겠습니다.", List.of("greeting-style"), "low"),
                    new LlmSuggestItem("정확한 확인을 위해 주문번호와 문제 내용을 알려주시면 신속히 도와드리겠습니다.", List.of("info-request"), "low")
            );
        }

        // 7) 저장
        int pr = 1;
        List<Recommend> saved = new ArrayList<>();
        for (var si : safe) {
            Recommend r = new Recommend(null, m, si.getText(), pr++);
            saved.add(recommendRepo.save(r));
        }

        // 8) 응답 DTO
        List<RecommendItemDto> out = new ArrayList<>();
        for (int i = 0; i < saved.size(); i++) {
            out.add(RecommendItemDto.builder()
                    .id(saved.get(i).getId())
                    .text(saved.get(i).getResponseText())
                    .priority(saved.get(i).getPriority())
                    .policyIds(safe.get(Math.min(i, safe.size() - 1)).getPolicy_ids())
                    .build());
        }
        return RecommendListDto.builder().messageId(messageId).items(out).build();
    }

    /** 모델별 변형/래핑 대응: 배열/객체+items, 기타 키 탐색 */
    private List<LlmSuggestItem> parseSuggestions(JsonNode res) {
        try {
            String content = res.path("choices").get(0).path("message").path("content").asText();
            String t = content == null ? "" : content.trim();

            // 디버깅: 처음 600자만
            if (log.isDebugEnabled()) {
                log.debug("[LLM-RAW] {}", t.length() > 600 ? t.substring(0, 600) + "..." : t);
            }

            if (t.startsWith("[")) {
                return om.readValue(t, new TypeReference<List<LlmSuggestItem>>() {});
            }
            if (t.startsWith("{")) {
                JsonNode n = om.readTree(t);
                // 1) items
                if (n.has("items") && n.get("items").isArray()) {
                    return om.readValue(n.get("items").toString(),
                            new TypeReference<List<LlmSuggestItem>>() {});
                }
                // 2) 일반적인 키들
                for (String k : List.of("recommendations","data","result")) {
                    if (n.has(k) && n.get(k).isArray()) {
                        return om.readValue(n.get(k).toString(),
                                new TypeReference<List<LlmSuggestItem>>() {});
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[LLM-PARSE-FAIL] {}", e.toString());
        }
        return List.of(); // → 폴백 트리거
    }
}
