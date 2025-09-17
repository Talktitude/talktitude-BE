package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.ChatMedia;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.common.util.DateTimeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatMediaResponse {
    private Long messageId;
    private String senderType;
    private long createdAt;
    private List<String> mediaUrls;

    public ChatMediaResponse(ChatMessage message, List<ChatMedia> medias) {
        this.messageId = message.getId();
        this.senderType = message.getSenderType().name();
        this.createdAt = DateTimeUtils.toEpochMillis(message.getCreatedAt());
        this.mediaUrls = medias.stream().map(ChatMedia::getMediaUrl).toList();
    }
}
