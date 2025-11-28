package edu.sookmyung.talktitude.chat.dto;

//TODO Order의 orderNumber가 중복이 없다는 것을 비즈니스로직 차원에서만 가정해도 옳은가
public record OrderHistory(
        Long orderId,
        String restaurantImageUrl,
        String restaurantName,
        String orderSummary,
        String orderDate
) {
}
