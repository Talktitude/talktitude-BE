package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.SenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
// 클라이언트에게 전달될 메시지
public class ChatMessageResponse {
    private Long messageId;
    private String originalText;
    private String convertedText;
    private String senderType;
    private LocalDateTime createdAt;
}
