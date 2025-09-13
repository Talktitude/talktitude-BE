package edu.sookmyung.talktitude.client.dto;

import edu.sookmyung.talktitude.client.model.DeliveryStatus;
import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.client.model.OrderDelivery;

import java.time.format.DateTimeFormatter;

public record OrderInfo(
        String orderDate,
        DeliveryStatus deliveryStatus,
        String restaurantImageUrl,
        String restaurantName,
        String orderNumber,
        boolean isCurrentOrder

) {
    public static OrderInfo convertToOrderInfo(Order order, OrderDelivery orderDelivery,boolean isCurrentOrder) {
        return new OrderInfo(
                order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm")),
                orderDelivery.getStatus(),
                order.getRestaurant().getImageUrl(),
                order.getRestaurant().getName(),
                order.getOrderNumber(),
                isCurrentOrder
        );
    }
}