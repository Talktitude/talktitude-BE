package edu.sookmyung.talktitude.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateTimeUtils {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private DateTimeUtils() {}

    // LocalDateTime -> Instant(UTC) -> epochMillis
    public static long toEpochMillis(LocalDateTime ldt) {
        if (ldt == null) return 0L;
        return ldt.atZone(KST). // KST로 해석
                toInstant(). // UTC Instant로 변환
                toEpochMilli(); // 숫자 ms
    }
}
