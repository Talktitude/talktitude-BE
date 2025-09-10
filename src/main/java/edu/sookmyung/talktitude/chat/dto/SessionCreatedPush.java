package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionCreatedPush {
    private Long sessionId;
    private String clientLoginId;
    private String clientPhone;
    private String profileImageUrl;
    private String status;
    private long lastMessageTime;
}
