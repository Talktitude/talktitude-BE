package edu.sookmyung.talktitude.common.response;

import edu.sookmyung.talktitude.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String code;
    private String message;



    public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<T>(true,data,"200","요청이 성공했습니다.");
    }

    public static <T> ApiResponse<T> ok(T data, String message){
        return new ApiResponse<T>(true,data,"200",message);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data){
        return new ApiResponse<>(false,data,errorCode.getCode(),errorCode.getMessage());
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode){
        return new ApiResponse<>(false,null,errorCode.getCode(),errorCode.getMessage());
    }
}
