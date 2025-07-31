package edu.sookmyung.talktitude.client.dto;

import edu.sookmyung.talktitude.client.model.Method;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInfo {
    private int paidAmount; //결제 금액
    private Method method;
    private int totalAmount; //총 금액
    private int menuPrice; //메뉴 금액
    private int deliveryFee; //배달팁
    private int discountAmount; //할인금액
    private int couponAmount; //쿠폰 금액
}
