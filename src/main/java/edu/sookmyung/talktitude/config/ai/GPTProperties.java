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
               다음 고객상담 대화를 분석하여 JSON 형식으로 응답해주세요.
                
                **중요: "category" 필드는 반드시 아래 9개 값 중 하나여야 합니다:**
                - 주문
                - 결제
                - 배송
                - 리뷰
                - 회원
                - 쿠폰
                - 서비스 이용
                - 안전 거래
                - 기타
                
                **카테고리 분류 기준:**
                - 주문 관련 문의, 취소, 환불 → "주문"
                - 결제 오류, 결제 수단 문제 → "결제"
                - 배송 지연, 배송지 변경, 배송 문제 → "배송"
                - 리뷰 작성, 평점, 후기 관련 → "리뷰"
                - 계정 문제, 회원가입, 로그인 → "회원"
                - 쿠폰 사용, 할인 혜택, 프로모션 → "쿠폰"
                - 앱/웹 사용법, 기능 문의 → "서비스 이용"
                - 보안, 사기 방지, 안전 관련 → "안전 거래"
                - 위에 해당하지 않는 모든 문의 → "기타"
                
                **요약 작성 규칙:**
               - 첫 번째 줄: 고객의 문제나 요청 사항
               - 두 번째 줄: 상담원의 해결 방안이나 답변 내용 \s
               - 세 번째 줄: 최종 처리 상태나 결과
               - 각 줄은 \\n으로 구분하여 작성
                
               응답: {"category":"카테고리","summary":"첫번째 문장\\n두번째 문장\\n세번째 문장"}
                
               대화 내용:
                """;
        private String model="gpt-3.5-turbo";
        private double temperature=0.1; //모델의 창의성과 무작위성을 조절 -> 0.3으로 예상 가능하고 일관된 답변 유도.
        private int maxTokens=400;

        private boolean streamResponse = false;
    }
}
