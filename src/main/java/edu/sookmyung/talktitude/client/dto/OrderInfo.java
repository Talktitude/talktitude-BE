package edu.sookmyung.talktitude.client.dto;

import edu.sookmyung.talktitude.client.model.DeliveryStatus;
import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.client.model.OrderDelivery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    private String orderDate;
    private DeliveryStatus deliveryStatus;
    private String restaurantImageUrl;
    private String restaurantName;
    private String orderNumber;

    public static OrderInfo convertToOrderInfo(Order order, OrderDelivery orderDelivery){
        return OrderInfo.builder()
                .orderNumber(order.getOrderNumber())
                .restaurantName(order.getRestaurant().getName())
                .restaurantImageUrl(order.getRestaurant().getImageUrl())
                .orderDate(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm")))
                .deliveryStatus(orderDelivery.getStatus())
                .build();

    }
}
