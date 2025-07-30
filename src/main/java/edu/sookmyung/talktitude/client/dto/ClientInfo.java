package edu.sookmyung.talktitude.client.dto;

import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.model.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientInfo {
    private String name;
    private String loginId;
    private String phoneNumber;
    private String address;
    private Long point;
    private int totalCouponCount;
    private List<CouponInfo> couponInfo;


    public static ClientInfo convertToClientInfo(Client client) {
        Long point = (client.getPoint() != null) ? client.getPoint().getAmount() : 0; //연관관계일 경우 null 체크 필요 -> fetch join으로 성능 개선 가능

       Map<Integer, Integer> couponAmountByCurrency = client.getCoupons().stream()
                         .collect(Collectors.groupingBy(Coupon::getCurrency,
                       Collectors.summingInt(Coupon::getAmount)));
        List<CouponInfo> coupons = couponAmountByCurrency.entrySet().stream()
                .map(entry-> CouponInfo.builder()
                        .currency(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
        return ClientInfo.builder().name(client.getName())
                .loginId(client.getLoginId())
                .phoneNumber(client.getPhone())
                .address(client.getAddress())
                .point(point)
                .couponInfo(coupons)
                .totalCouponCount(coupons.stream().mapToInt(CouponInfo::getAmount).sum())
                .build();
    }
}
