package edu.sookmyung.talktitude.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ClientChatSessionDto {
    private Long sessionId;
    private String status;         // "IN_PROGRESS" | "FINISHED"
    private String storeName;
    private String storeImageUrl;
    private String orderSummary;   // 예) "아기만두 외 3개" / "주문 외 문의"
    private int totalPrice;        // 없으면 0
    private String lastMessage;    // 클라이언트 관점 textToShow
}
