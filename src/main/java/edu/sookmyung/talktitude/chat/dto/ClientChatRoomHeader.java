package edu.sookmyung.talktitude.chat.dto;

import edu.sookmyung.talktitude.chat.model.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClientChatRoomHeader {
    private Long sessionId;
    private Long orderId; // 주문 연결 없으면 null
    private String title; // 가게명 또는 "주문 외 문의"
    private boolean orderLinked; // 주문 연결 여부
    private Status status;
}
