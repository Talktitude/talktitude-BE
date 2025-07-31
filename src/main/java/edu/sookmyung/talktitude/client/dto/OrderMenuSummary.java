package edu.sookmyung.talktitude.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class OrderMenuSummary {
    private List<OrderMenuInfo> orderMenuInfos;
    private int totalPrice; //총 금액
}
