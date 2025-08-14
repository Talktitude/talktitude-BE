package edu.sookmyung.talktitude.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClientChatSessionListResponse {
    private int inProgressCount;
    private int finishedCount;
    private List<ClientChatSessionDto> inProgress; // 상담중 목록
    private List<ClientChatSessionDto> finished;   // 상담종료 목록
}
