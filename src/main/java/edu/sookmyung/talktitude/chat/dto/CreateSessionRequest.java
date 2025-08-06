package edu.sookmyung.talktitude.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateSessionRequest {
    private Long clientId;
    private Long orderId; // null이면 '주문 외 문의'
}
