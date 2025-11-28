package edu.sookmyung.talktitude.client.dto;


public record OrderMenuInfo (
     String menuName,
     int menuQuantity,
     int menuPrice, // 메뉴 1개당 가격
     int totalMenuPrice //menuQuantity * menuPrice
) {}
