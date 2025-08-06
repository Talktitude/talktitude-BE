package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.client.model.Client;
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
    private LocalDateTime createdAt;
    private String status;

    public ChatSessionDetailDto(ChatSession session, Client client) {
        this.sessionId = session.getId();
        this.clientLoginId = client.getLoginId();
        this.clientName = client.getName();
        this.clientPhone = client.getPhone();
        this.createdAt = session.getCreatedAt();
        this.status = session.getStatus().name();
    }
}
