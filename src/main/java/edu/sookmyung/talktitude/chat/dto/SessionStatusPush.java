package edu.sookmyung.talktitude.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionStatusPush {
    private Long sessionId;
    private String status; // "IN_PROGRESS" | "FINISHED"
}
