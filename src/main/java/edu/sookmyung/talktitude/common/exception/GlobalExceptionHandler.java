package edu.sookmyung.talktitude.common.exception;

import edu.sookmyung.talktitude.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value={BaseException.class})
    public ResponseEntity<?> handleBaseException(BaseException e){
        log.error("비즈니스 예외: {} - {}",e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(e.getErrorCode()));
    }

    @ExceptionHandler(value={UsernameNotFoundException.class})
    public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException e){
        log.error("사용자 인증 실패(UsernameNotFoundException) : {} ",e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTHENTICATION_FAILED));
    }

    @ExceptionHandler(value={BadCredentialsException.class})
    public ResponseEntity<?> handleBadCredentials(UsernameNotFoundException e){
        log.error("사용자 인증 실패(BadCredentialsException) : {} ",e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTHENTICATION_FAILED));
    }


    // Spring MVC 필수 예외들
    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleSpringMvcExceptions(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT));
    }

    // 최종 안전망
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        log.error("예상치 못한 예외: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

}
