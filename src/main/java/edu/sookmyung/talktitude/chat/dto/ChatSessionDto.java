package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatSessionDto {
    private Long sessionId;
    private String clientLoginId;
    private String clientPhone;
    private String profileImageUrl;
    private Status status;
    private long lastMessageTime;
}
