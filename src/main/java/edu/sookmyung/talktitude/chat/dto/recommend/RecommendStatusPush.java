package edu.sookmyung.talktitude.chat.dto.recommend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendStatusPush {
    private Long messageId;
    private String state; // "STARTED" | "DONE" | "ERROR"
    private String reason;
    private long timestamp; // UTC epoch(ms)
}
