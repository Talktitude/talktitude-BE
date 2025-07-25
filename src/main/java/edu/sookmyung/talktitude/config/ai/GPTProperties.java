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
        private String politePrompt ="다음 메시지를 정중하고 예의바른 표현으로 변환해주세요. 원본의 의미는 유지하되 더 공손한 톤으로 작성해주세요:\\n\n";
        private String model="gpt-3.5-turbo";
        private double temperature=0.5;
        private int maxTokens=1000;
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
