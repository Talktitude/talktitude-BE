package edu.sookmyung.talktitude.client.dto;

import edu.sookmyung.talktitude.client.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDetailInfo {

    //기본 주문 정보
    private OrderInfo orderInfo;

    //주문 메뉴
    private OrderMenuSummary orderMenuSummary;

    //결제 정보( menuPrice 포함 )
    private PaymentInfo paymentInfo;

    //배달 정보
    private DeliveryInfo deliveryInfo;

    public static OrderDetailInfo convertToOrderDetailInfo(Order order, OrderDelivery orderDelivery, OrderPayment orderPayment,List<OrderMenu> orderMenus){
        List<OrderMenuInfo> orderMenuInfos = orderMenus.stream().map(menu->
                OrderMenuInfo.builder()
                        .menuName(menu.getMenu())
                        .menuQuantity(menu.getQuantity())
                        .menuPrice(menu.getPrice())
                        .totalMenuPrice(menu.getQuantity()*menu.getPrice())
                        .build())
                .collect(Collectors.toList());


        return OrderDetailInfo.builder()
                .orderInfo(OrderInfo.builder()
                        .restaurantName(order.getRestaurant().getName())
                        .orderNumber(order.getOrderNumber())
                        .restaurantImageUrl(order.getRestaurant().getImageUrl())
                        .orderDate(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm")))
                        .deliveryStatus(orderDelivery.getStatus())
                        .build())
                .deliveryInfo(DeliveryInfo.builder()
                        .deliveryNote(orderDelivery.getDeliveryNote())
                        .phone(orderDelivery.getPhone())
                        .address(orderDelivery.getAddress())
                        .restaurantNote(orderDelivery.getRestaurantNote())
                        .build())
                .paymentInfo(PaymentInfo.builder()
                        .paidAmount(orderPayment.getPaidAmount())
                        .method(orderPayment.getMethod())
                        .totalAmount(orderPayment.getTotalAmount())
                        .menuPrice(orderPayment.getMenuPrice())
                        .deliveryFee(orderPayment.getDeliveryFee())
                        .discountAmount(orderPayment.getDiscountAmount())
                        .couponAmount(orderPayment.getCouponAmount())
                        .build())
                .orderMenuSummary(OrderMenuSummary.builder()
                        .totalPrice(orderPayment.getMenuPrice())
                        .orderMenuInfos(orderMenuInfos)
                        .build())
                .build();
    }
}
