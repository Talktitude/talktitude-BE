package edu.sookmyung.talktitude.client.dto;

import edu.sookmyung.talktitude.client.model.Method;

public record PaymentInfo(
        int paidAmount,
        Method method,
        int totalAmount,
        int menuPrice,
        int deliveryFee,
        int discountAmount,
        int couponAmount
) {
}