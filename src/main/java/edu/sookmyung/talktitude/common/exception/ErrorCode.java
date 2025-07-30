package edu.sookmyung.talktitude.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //토큰 관련
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"INVALID_TOKEN","유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,"TOKEN_EXPIRED","만료된 토큰입니다."),
    WRONG_USRETYPE(HttpStatus.BAD_REQUEST,"WRONG_USERTYPE","잘못된 사용자 타입입니다."),

    // Spring Security 관련
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED,"AUTHENTICATION_FAILED","사용자 인증에 실패하였습니다."),
    WRONG_CREDENTIALS(HttpStatus.UNAUTHORIZED,"WRONG_CREDENTIALS","아이디 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","인증되지 않은 사용자입니다."), // ✅ 추가

    //Client 관련
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CLIENT_001", "고객 정보를 찾을 수 없습니다."),

    //Member 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "사용자 정보를 찾을 수 없습니다."),

    //ChatSession 관련
    CHATSESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATSESSION_001", "세션 정보를 찾을 수 없습니다."),

    // Order 관련
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_O001", "주문을 찾을 수 없습니다."),
    ORDER_DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_O002", "배달 정보를 찾을 수 없습니다."),
    ORDER_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_O003", "결제 정보를 찾을 수 없습니다."),

    //Report 관련
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_001", "리포트 정보를 찾을 수 없습니다."),
    REPORT_JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT_002","리포트 생성 중 JSON 처리에 실패하였습니다"),
    GPT_API_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "Report_003", "GPT AI 서비스 호출에 실패했습니다."),


    //Memo 관련
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMO_001", "메모 정보를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN,"MEMO_002","메모 소유주가 아닙니다"),

    //Spring MVC 관련
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT","잘못된 요청입니다."),


    // 권한 관련
    RESOURCE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RES_001", "해당 리소스에 접근할 권한이 없습니다"),


    //최종 안전망
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR","예상치 못한 오류입니다");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
