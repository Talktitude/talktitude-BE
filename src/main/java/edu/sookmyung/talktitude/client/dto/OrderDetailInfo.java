package edu.sookmyung.talktitude.client.dto;

import edu.sookmyung.talktitude.client.model.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public record OrderDetailInfo(
        OrderInfo orderInfo,
        OrderMenuSummary orderMenuSummary,
        PaymentInfo paymentInfo,
        DeliveryInfo deliveryInfo
) {
    public static OrderDetailInfo convertToOrderDetailInfo(Order order, OrderDelivery orderDelivery, OrderPayment orderPayment, List<OrderMenu> orderMenus) {
        List<OrderMenuInfo> orderMenuInfos = orderMenus.stream().map(menu ->
                new OrderMenuInfo(
                        menu.getMenu(),
                        menu.getQuantity(),
                        menu.getPrice(),
                        menu.getQuantity()* menu.getPrice()
                ))
                .collect(Collectors.toList());

        return new OrderDetailInfo(
                new OrderInfo(
                        order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm")),
                        orderDelivery.getStatus(),
                        order.getRestaurant().getImageUrl(),
                        order.getRestaurant().getName(),
                        order.getOrderNumber()
                ),
                new OrderMenuSummary(
                        orderMenuInfos,
                        orderPayment.getMenuPrice()
                ),
                new PaymentInfo(
                        orderPayment.getPaidAmount(),
                        orderPayment.getMethod(),
                        orderPayment.getTotalAmount(),
                        orderPayment.getMenuPrice(),
                        orderPayment.getDeliveryFee(),
                        orderPayment.getDiscountAmount(),
                        orderPayment.getCouponAmount()
                ),
                new DeliveryInfo(
                        orderDelivery.getPhone(),
                        orderDelivery.getAddress(),
                        orderDelivery.getDeliveryNote(),
                        orderDelivery.getRestaurantNote()
                )
        );
    }
}