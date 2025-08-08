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

    //요청 관련
    WRONG_USERTYPE(HttpStatus.BAD_REQUEST,"REQ_001","잘못된 사용자 타입입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "REQ_002","지원하지 않는 Http 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "REQ_003", "요청한 경로를 찾을 수 없습니다"),
    CHAT_SESSION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "REQ_004", "해당 채팅 세션에 접근할 권한이 없습니다"),

    // Spring Security 관련
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED,"AUTHENTICATION_FAILED","사용자 인증에 실패하였습니다."),
    WRONG_CREDENTIALS(HttpStatus.UNAUTHORIZED,"WRONG_CREDENTIALS","아이디 또는 비밀번호가 올바르지 않습니다."),

    //Client 관련
    CLIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CLIENT_001", "고객 정보를 찾을 수 없습니다."),

    //Member 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "사용자 정보를 찾을 수 없습니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "MEMBER_002", "이미 존재하는 로그인 ID입니다."),

    //ChatSession 관련
    CHATSESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATSESSION_001", "세션 정보를 찾을 수 없습니다."),
    INVALID_SESSION_STATE(HttpStatus.BAD_REQUEST, "CHATSESSION_002", "이미 종료된 상담 세션입니다."),
    CHATSESSION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHATSESSION_003", "해당 세션에 접근할 권한이 없습니다."),

    // Order 관련
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_O01", "주문을 찾을 수 없습니다."),
    ORDER_DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_O02", "배달 정보를 찾을 수 없습니다."),
    ORDER_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_O03", "결제 정보를 찾을 수 없습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORDER_O04", "해당 주문에 접근할 권한이 없습니다."),

    //Report 관련
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_001", "리포트 정보를 찾을 수 없습니다."),
    REPORT_JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT_002","리포트 생성 중 JSON 처리에 실패하였습니다"),
    GPT_API_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "REPORT_003", "GPT AI 서비스 호출에 실패했습니다."),
    REPORT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "REPORT_004", "해당 고객의 리포트 내용에 접근할 권한이 없습니다."),

    //Memo 관련
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMO_001", "메모 정보를 찾을 수 없습니다."),
    MEMO_ACCESS_DENIED(HttpStatus.FORBIDDEN,"MEMO_002","메모 소유주가 아닙니다."),

    //Spring MVC 관련
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST","요청 형식이 잘못되었습니다."),

    //최종 안전망
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR","예상치 못한 오류입니다");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
