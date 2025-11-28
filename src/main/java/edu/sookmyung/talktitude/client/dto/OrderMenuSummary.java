package edu.sookmyung.talktitude.client.dto;

import java.util.List;

public record OrderMenuSummary(
        List<OrderMenuInfo> orderMenuInfos,
        int totalPrice
) {
}