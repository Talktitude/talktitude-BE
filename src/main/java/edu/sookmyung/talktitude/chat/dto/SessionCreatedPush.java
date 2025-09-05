package edu.sookmyung.talktitude.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SessionCreatedPush {
    private Long sessionId;
    private String clientLoginId;
    private String clientPhone;
    private String profileImageUrl;
    private LocalDateTime lastMessageTime;
}
