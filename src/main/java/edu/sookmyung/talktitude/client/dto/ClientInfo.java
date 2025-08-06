package edu.sookmyung.talktitude.client.dto;

import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.model.Coupon;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ClientInfo(
        String name,
        String loginId,
        String phoneNumber,
        String address,
        Long point,
        int totalCouponCount,
        List<CouponInfo> couponInfo
) {
    public static ClientInfo convertToClientInfo(Client client) {
        Long point = (client.getPoint() != null) ? client.getPoint().getAmount() : 0; //연관관계일 경우 null 체크 필요 -> fetch join으로 성능 개선 가능

        Map<Integer, Integer> couponAmountByCurrency = client.getCoupons().stream()
                .collect(Collectors.groupingBy(Coupon::getCurrency,
                        Collectors.summingInt(Coupon::getAmount)));

        List<CouponInfo> coupons = couponAmountByCurrency.entrySet().stream()
                .map(entry -> new CouponInfo(
                        entry.getKey(),
                        entry.getValue()
                        ))
                .collect(Collectors.toList());

        int totalCouponCount = coupons.stream().mapToInt(CouponInfo::amount).sum();

        return new ClientInfo(
                client.getName(),
                client.getLoginId(),
                client.getPhone(),
                client.getAddress(),
                point,
                totalCouponCount,
                coupons
        );
    }
}