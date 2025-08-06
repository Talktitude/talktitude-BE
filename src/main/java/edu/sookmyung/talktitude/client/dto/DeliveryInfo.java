package edu.sookmyung.talktitude.client.dto;

public record DeliveryInfo(
        String phone,
        String address,
        String deliveryNote,
        String restaurantNote
) {
}