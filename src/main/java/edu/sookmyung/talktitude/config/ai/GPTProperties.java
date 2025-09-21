package edu.sookmyung.talktitude.config.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
public class GPTProperties {

    private PoliteConfig polite = new PoliteConfig();
    private SummaryConfig summary = new SummaryConfig();

    @Data
    public static class PoliteConfig{
        //private String politePrompt ="다음 메시지를 정중하고 예의바른 표현으로 변환해주세요. 원본의 의미는 유지하되 더 공손한 톤으로 작성해주세요:\\n\n";
        private String politePrompt =
                    """
                    당신은 한국어 고객 응대 전문가이자 텍스트 변환 AI입니다.
                    입력 문장은 고객이 배달 서비스에서 남긴 불공손한 표현입니다.
                    이 표현은 겉으로는 존댓말이지만 실제로는 무례하거나 공격적인 의도가 담겨 있을 수 있습니다.
                    
                    ### 지시 사항
                    1. 반드시 고객의 입장에서, 고객이 직접 말하는 것처럼 변환하세요.
                    2. 절대로 상담원이나 제3자의 관점에서 응답하지 마세요.
                    
                    
                    ### 작업 지시
                    1. 문장의 맥락과 의도(불만, 환불 요청, 지연 문의 등)를 파악하세요.
                    2. 공격적이거나 비꼬는 부분은 제거하거나 완화하세요.
                    3. 문장의 핵심 요청/불만 내용은 그대로 유지하세요.
                    4. 결과는 자연스럽고 정중한 고객 표현으로 돌려 말하세요.
                    5. 반드시 JSON 형식으로만 응답하세요.
                    
                    ### 출력 형식 (JSON)
                    {"message":"<공손하게 변환된 문장>"}
                    
                    ### 예시
                    입력: "한 시간 넘게 기다리게 해놓고도 이 모양이신가요?"
                    출력: {"message":"한 시간 정도 지났는데, 현재 배달 상황을 알려주실 수 있을까요?"}
                    
                    입력: "이렇게 장사하시면 금방 망하실 텐데요."
                    출력: {"message":"서비스 개선이 필요할 것 같습니다. 피드백 반영 부탁드립니다."}
                    
                    대화 맥락을 참고하여 공손하게 변환하세요:
                    """;

        private String model="gpt-4";
        private double temperature=0.1;
        private int maxTokens=256;
    }


    @Data
    public static class SummaryConfig{
        private String summaryPrompt = """
                Analyze the following customer service conversation and respond in JSON format.
                
                **IMPORTANT: The "category" field must be EXACTLY one of these 9 values:**
                - 주문
                - 결제
                - 배달
                - 리뷰
                - 회원
                - 쿠폰
                - 서비스이용
                - 안전거래
                - 기타
                
                **Category Guidelines:**
                - Order issues, cancellations, refunds → "주문"
                - Payment problems → "결제"
                - Delivery issues → "배달"
                - Reviews, ratings → "리뷰"
                - Account/membership → "회원"
                - Coupons, promotions → "쿠폰"
                - Service usage questions → "서비스이용"
                - Safety, security concerns → "안전거래"
                - Anything else → "기타"
                
                
                **Summary Guidelines:**
                  - 고객의 핵심 문제나 요청사항을 명확히 기술
                  - 상담원의 주요 해결방안이나 답변 내용 포함
                  - 최종 처리 결과나 상태 명시 (해결완료/진행중/추가조치필요 등)
                  - 2-3문장으로 간결하게 작성
                  - 고객과 상담원의 핵심 대화 내용만 포함
                  - 불필요한 인사말이나 반복적인 내용은 제외

                **Response format:**
                {
                    "category": "one of the 9 exact values above",
                    "summary": "고객 문제: [핵심 이슈]. 상담원 대응: [주요 해결방안]. 처리 결과: [최종 상태]. (각 섹션 사이는 한 칸 띄어쓰기로 구분하여 가독성 높게 작성)"
                }
                
                Customer service conversation:""";
        private String model="gpt-4";
        private double temperature=0.3; //모델의 창의성과 무작위성을 조절 -> 0.3으로 예상 가능하고 일관된 답변 유도.
        private int maxTokens=1000;
    }
}
