package edu.sookmyung.talktitude.client.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OrderMenuInfo {
    private String menuName;
    private int menuQuantity;
    private int menuPrice; // 메뉴 1개당 가격
    private int totalMenuPrice; //menuQuantity * menuPrice
}
