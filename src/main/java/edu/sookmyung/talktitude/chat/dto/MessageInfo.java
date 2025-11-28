package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.SenderType;
import edu.sookmyung.talktitude.common.util.DateTimeUtils;
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
    private long time;

    public static MessageInfo convertToMessageInfo(ChatMessage message){
        return MessageInfo.builder()
                .senderType(message.getSenderType())
                .convertedText(message.getConvertedText())
                .originalText(message.getOriginalText())
                .time(DateTimeUtils.toEpochMillis(message.getCreatedAt()))
                .build();
    }

}
