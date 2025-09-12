package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.common.util.DateTimeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatSessionDetailDto {
    private Long sessionId;
    private String clientLoginId;
    private String clientName;
    private String clientPhone;
    private long createdAt;
    private String status;

    private boolean orderRelated;
    private Long orderId;
    private String orderNumber;
    private String storeName;

    public ChatSessionDetailDto(ChatSession session, Client client) {
        this.sessionId = session.getId();
        this.clientLoginId = client.getLoginId();
        this.clientName = client.getName();
        this.clientPhone = client.getPhone();
        this.createdAt = DateTimeUtils.toEpochMillis(session.getCreatedAt());
        this.status = session.getStatus().name();
    }
}
