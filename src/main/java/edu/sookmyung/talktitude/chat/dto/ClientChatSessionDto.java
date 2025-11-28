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
    private String OrderSummary;  // 가격 포함
    private String lastMessage;    // 클라이언트 관점 textToShow
    private long lastMessageTime;
}
