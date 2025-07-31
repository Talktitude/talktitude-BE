package edu.sookmyung.talktitude.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryInfo {
    private String phone;
    private String address;
    private String deliveryNote;
    private String restaurantNote;
}
