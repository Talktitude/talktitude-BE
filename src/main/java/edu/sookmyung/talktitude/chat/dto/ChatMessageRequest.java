package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.SenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
// 클라이언트가 보내는 메시지
public class ChatMessageRequest {
    private Long sessionId;
    private String originalText;
    private SenderType senderType;
}
