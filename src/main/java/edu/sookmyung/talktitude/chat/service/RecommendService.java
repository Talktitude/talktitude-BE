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
import edu.sookmyung.talktitude.chat.repository.ChatMessageRepository;
import edu.sookmyung.talktitude.chat.repository.RecommendRepository;
import edu.sookmyung.talktitude.chat.recommend.facts.OrderFactsClient;
import edu.sookmyung.talktitude.chat.recommend.intent.IntentService;
import edu.sookmyung.talktitude.chat.recommend.kb.Retriever;
import edu.sookmyung.talktitude.config.ai.GptClient;
import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final ChatMessageRepository messageRepo;
    private final RecommendRepository recommendRepo;
    private final IntentService intentService;
    private final Retriever retriever;
    private final OrderFactsClient factsClient;
    private final GptClient gpt;
    private final SimpMessagingTemplate messaging;

    private final ObjectMapper om = new ObjectMapper();

    @Value("${recommend.generate.topk:4}") private int topK;
    @Value("${recommend.generate.n:4}") private int N;
    @Value("${recommend.generate.biz-days:3}") private int bizDays;

    @Transactional(readOnly = true)
    public RecommendListDto list(Long messageId) {
        List<Recommend> rows = recommendRepo.findByMessage_IdOrderByPriorityAsc(messageId);
        List<RecommendItemDto> items = rows.stream()
                .map(r -> RecommendItemDto.builder()
                        .id(r.getId())
                        .text(r.getResponseText())
                        .priority(r.getPriority())
                        .policyIds(Collections.emptyList()) // RecommendSource 연결 시 채우기
                        .build())
                .toList();
        return RecommendListDto.builder().messageId(messageId).items(items).build();
    }

    /** 메시지 저장 직후 비동기로 호출해 생성 → 저장 → WebSocket push */
    @Async
    @Transactional
    public void generateAndPush(Long messageId) {
        RecommendListDto dto = generate(messageId);
        // 세션 사용자 큐로 push
        ChatMessage m = messageRepo.findById(messageId).orElseThrow(() -> new BaseException(ErrorCode.INVALID_REQUEST));
        ChatSession s = m.getChatSession();
        String agentLoginId  = s.getMember().getLoginId();
        String clientLoginId = s.getClient().getLoginId();
        String dest = "/queue/chat/" + s.getId() + "/recommendations";
        messaging.convertAndSendToUser(agentLoginId, dest, dto);
        // 고객에게도 보여줄지 정책에 따라 결정(보통 상담원만)
    }

    /** 동기 생성(REST로 즉시 요청 시) */
    @Transactional
    public RecommendListDto generate(Long messageId) {
        ChatMessage m = messageRepo.findById(messageId)
                .orElseThrow(() -> new BaseException(ErrorCode.INVALID_REQUEST));

        // 이미 있으면 바로 반환(중복 생성 방지)
        List<Recommend> existing = recommendRepo.findByMessage_IdOrderByPriorityAsc(messageId);
        if (!existing.isEmpty()) {
            return list(messageId);
        }

        // 1) intent
        String intent = intentService.classify(m.getOriginalText());

        // 2) order facts
        Order order = m.getChatSession().getOrder(); // null 허용
        Map<String,Object> facts = factsClient.buildFacts(m, order);

        // 3) RAG retrieve
        var docs = retriever.retrieve(m.getOriginalText(), intent, topK);

        // 4) LLM 호출
        JsonNode payload = new edu.sookmyung.talktitude.chat.recommend.llm.PromptBuilder()
                .build(docs, m.getOriginalText(), intent, facts, N, bizDays);
        JsonNode res = gpt.chat(payload);

        // 5) 파싱
        List<LlmSuggestItem> items = parseSuggestions(res);

        // 6) 필터링(안전/길이)
        List<LlmSuggestItem> safe = items.stream()
                .filter(it -> it.getText()!=null && it.getText().length()<=400)
                .filter(it -> !"high".equalsIgnoreCase(it.getRisk()))
                .collect(Collectors.toList());

        if (safe.isEmpty()) { // 폴백 템플릿
            safe = List.of(new LlmSuggestItem("불편을 드려 죄송합니다. 주문 내역을 확인 후 가능한 조치를 안내드리겠습니다.", List.of("greeting-style"), "low"));
        }

        // 7) 저장
        int pr = 1;
        List<Recommend> saved = new ArrayList<>();
        for (var si : safe) {
            Recommend r = new Recommend(null, m, si.getText(), pr++);
            saved.add(recommendRepo.save(r));
            // (선택) 근거 정책 저장
        }

        // 8) 응답 DTO
        List<RecommendItemDto> out = new ArrayList<>();
        for (int i=0;i<saved.size();i++) {
            out.add(RecommendItemDto.builder()
                    .id(saved.get(i).getId())
                    .text(saved.get(i).getResponseText())
                    .priority(saved.get(i).getPriority())
                    .policyIds(safe.get(Math.min(i, safe.size()-1)).getPolicy_ids())
                    .build());
        }
        return RecommendListDto.builder().messageId(messageId).items(out).build();
    }

    private List<LlmSuggestItem> parseSuggestions(JsonNode res) {
        try {
            String content = res.path("choices").get(0).path("message").path("content").asText();
            // 모델이 배열 또는 객체로 줄 수 있음 → 유연 파싱
            if (content.trim().startsWith("[")) {
                return om.readValue(content, new TypeReference<List<LlmSuggestItem>>() {});
            } else if (content.trim().startsWith("{")) {
                // {"items":[...]} 형태 가정
                JsonNode n = om.readTree(content);
                if (n.has("items")) {
                    return om.readValue(n.get("items").toString(), new TypeReference<List<LlmSuggestItem>>() {});
                }
            }
        } catch (Exception ignore) {}
        return List.of();
    }
}
