package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.SenderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
// 클라이언트에게 전달될 메시지
public class ChatMessageResponse {
    private Long messageId;
    private String textToShow;    // 화면에 보여지는 텍스트(상담원: 원문, 고객: 공손화된 메시지 or 원문(공손화 필요X 경우))
    private String originalText;  // 원문 텍스트(고객이 직접 입력한 것, 공손화된 경우에만 제공)
    private boolean showOriginal; // 원문보기 버튼 표시 여부
    private String senderType;
    private LocalDateTime createdAt;

    public ChatMessageResponse(ChatMessage message, String userType) {
        this.messageId = message.getId();
        this.originalText = message.getOriginalText();
        this.senderType = message.getSenderType().name();
        this.createdAt = message.getCreatedAt();

        if ("MEMBER".equalsIgnoreCase(userType)) {
            // 상담원 화면: 공손화가 있으면 공손문, 없으면 원문
            this.textToShow  = (message.getConvertedText() != null)
                    ? message.getConvertedText()
                    : message.getOriginalText();
            this.showOriginal = (message.getConvertedText() != null); // 상담원만 원문보기 버튼
        } else {
            // 고객 화면: 항상 본인이 쓴 원문만
            this.textToShow  = message.getOriginalText();
            this.showOriginal = false;
        }
    }
}
