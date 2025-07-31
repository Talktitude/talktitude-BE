package edu.sookmyung.talktitude.client.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CouponInfo {
    private int currency;
    private int amount;
}
