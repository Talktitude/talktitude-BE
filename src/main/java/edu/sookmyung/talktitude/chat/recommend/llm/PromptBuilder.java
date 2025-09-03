package edu.sookmyung.talktitude.chat.recommend.llm;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.sookmyung.talktitude.chat.recommend.kb.KnowledgeBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class PromptBuilder {

    private final ObjectMapper om = new ObjectMapper();

    @Value("${ai.openai.chat-model:gpt-4o-mini}")
    private String chatModel;

    public ObjectNode build(List<KnowledgeBase.Doc> docs,
                            String customerText,
                            String intent,
                            Map<String, Object> facts,
                            int n,
                            int bizDays) {

        ArrayNode msgs = om.createArrayNode();

        // ===== system message =====
        String systemContent =
                "너는 '배달 고객센터 상담원 보조'야.\n" +
                        "- 사실(주문/결제)과 아래 정책 스니펫만 사용해 답변을 만든다.\n" +
                        "- 환불/보상은 '조건부/절차'로만 안내하고 확정 약속은 하지 않는다.\n" +
                        "- 2~3문장, 존댓말, 사과→다음 조치→재문의 유도.\n" +
                        "- 개인정보 요구 금지. 민감한 표현/비난 금지.\n" +
                        "- 반드시 JSON 배열을 반환: [{\"text\":\"...\",\"policy_ids\":[\"...\"],\"risk\":\"low\"}]";
        msgs.add(obj("role", "system", "content", systemContent));

        // ===== docs as JSON array string =====
        ArrayNode docArr = om.createArrayNode();
        for (KnowledgeBase.Doc d : docs) {
            ObjectNode o = om.createObjectNode();
            if (d.id != null) o.put("id", d.id);
            if (d.category != null) o.put("category", d.category);
            if (d.policy != null) o.put("policy", d.policy);
            if (d.script != null) o.put("script", d.script);
            if (d.steps != null)  o.set("steps", om.valueToTree(d.steps));
            if (d.templates != null) o.set("templates", om.valueToTree(d.templates));
            docArr.add(o);
        }

        String userContent =
                "고객 메시지:\n" + safe(customerText) + "\n\n" +
                        "의도(intent): " + safe(intent) + "\n" +
                        "사실값(facts): " + facts + "\n" +
                        "정책 스니펫(policy_snippets): " + docArr.toString() + "\n\n" +
                        "요청:\n" +
                        "- 서로 다른 전략의 추천 답변 " + n + "개를 만들어라.\n" +
                        "- (1) 즉시 안내 (2) 확인 후 콜백 (3) 증빙 요청 (4) 대안/절차 안내 등 다양하게.\n" +
                        "- 각 답변은 2~3문장.\n" +
                        "- 환불/보상/쿠폰은 확정 약속 금지, 절차만.\n" +
                        "- 영업일 표기는 기본 " + bizDays + "일을 사용해도 된다.\n" +
                        "- JSON 배열만 출력. 설명 금지.";
        msgs.add(obj("role", "user", "content", userContent));

        ObjectNode payload = om.createObjectNode();
        payload.put("model", chatModel);                  // 프로퍼티에서 읽음
        payload.set("messages", msgs);
        payload.put("temperature", 0.2);

        // 일부 모델에서만 강제 JSON 지원. 호환 안 되면 주석 처리해도 됨.
        ObjectNode rf = om.createObjectNode();
        rf.put("type", "json_object");
        payload.set("response_format", rf);

        return payload;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private ObjectNode obj(String k1, String v1, String k2, String v2) {
        ObjectNode o = new ObjectNode(JsonNodeFactory.instance);
        o.put(k1, v1);
        o.put(k2, v2);
        return o;
    }
}