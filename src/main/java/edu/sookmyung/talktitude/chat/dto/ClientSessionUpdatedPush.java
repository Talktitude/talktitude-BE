package edu.sookmyung.talktitude.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientSessionUpdatedPush {
    private Long sessionId;
    private String status;
    private String storeName;
    private String storeImageUrl;
    private String orderSummary;
    private String lastMessage;
    private long lastMessageTime;
}
