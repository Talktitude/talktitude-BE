package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageInfo {
    private SenderType senderType;
    private String originalText;
    private String convertedText;
    private LocalDateTime time;

    public static MessageInfo convertToMessageInfo(ChatMessage message){
        return MessageInfo.builder()
                .senderType(message.getSenderType())
                .convertedText(message.getConvertedText())
                .originalText(message.getOriginalText())
                .time(message.getCreatedAt())
                .build();
    }

}
