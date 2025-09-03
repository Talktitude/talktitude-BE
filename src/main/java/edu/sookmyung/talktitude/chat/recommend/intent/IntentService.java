package edu.sookmyung.talktitude.chat.recommend.intent;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class IntentService {

    private final Map<String, List<String>> rules = Map.of(
            "delay", List.of("느려", "늦", "언제오", "배달 왜", "지연"),
            "missing", List.of("누락", "빠졌", "안왔", "안 왔", "없어"),
            "wrong", List.of("오배송", "다른", "잘못", "엉뚱"),
            "quality", List.of("차갑", "미지근", "탄맛", "싱거", "위생", "상했"),
            "refund", List.of("환불", "취소", "돈", "결제 취소"),
            "redelivery", List.of("재배달", "다시 보내"),
            "coupon", List.of("쿠폰", "보상"),
            "payment", List.of("결제", "중복", "영수증", "현금영수증"),
            "address", List.of("주소", "잘못", "다른 곳"),
            "app", List.of("앱", "로그인", "오류", "버그")
    );

    public String classify(String text) {
        if (text == null) return "other";
        String t = text.toLowerCase();
        for (var e : rules.entrySet()) {
            for (String kw : e.getValue()) {
                if (t.contains(kw)) return e.getKey();
            }
        }
        return "other";
    }
}
