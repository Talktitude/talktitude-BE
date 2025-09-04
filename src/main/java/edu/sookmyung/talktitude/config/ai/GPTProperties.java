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
                "당신은 배달서비스 고객센터의 AI입니다. 고객이 상담원에게 보내는 메시지를 분석하여, 불공손한 표현만 적절히 완화해주세요.\n\n" +

                        "** 중요 원칙 **\n" +
                        "1. 고객의 감정과 불만의 강도는 상담원이 알 수 있도록 어느 정도 유지해주세요\n" +
                        "2. 구체적인 문제점이나 요구사항은 절대 바꾸지 마세요\n" +
                        "3. 배달 관련 상황임을 고려하여 자연스럽게 변환해주세요\n\n" +

                        "불공손 기준: 욕설, 반말, 인격모독, 과도한 공격성\n\n" +

                        "예시:\n" +
                        "- \"야 뭐하냐\" → \"지금 상황이 어떻게 되는 건가요?\"\n" +
                        "- \"진짜 짜증나\" → \"정말 답답합니다\"\n" +
                        "- \"일처리 이따구로\" → \"업무 처리가 이런 식인가요?\"\n\n" +

                        "응답 형식:\n" +
                        "{\"label\": \"polite/impolite\", \"message\": \"변환된_메시지\"}\n\n" +

                        "대화 맥락을 참고하여 판단하세요:\n";

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
                
                **Response format:**
                {
                    "category": "one of the 9 exact values above",
                    "summary": "brief summary in Korean"
                }
                
                Customer service conversation:""";
        private String model="gpt-3.5-turbo";
        private double temperature=0.3; //모델의 창의성과 무작위성을 조절 -> 0.3으로 예상 가능하고 일관된 답변 유도.
        private int maxTokens=1000;
    }
}
