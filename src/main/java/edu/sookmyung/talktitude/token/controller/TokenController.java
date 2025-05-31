package edu.sookmyung.talktitude.token.controller;

import edu.sookmyung.talktitude.exception.InvalidTokenException;
import edu.sookmyung.talktitude.exception.TokenExpiredException;
import edu.sookmyung.talktitude.token.dto.CreateAccessTokenRequest;
import edu.sookmyung.talktitude.token.dto.CreateAccessTokenResponse;
import edu.sookmyung.talktitude.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class TokenController {

    private final TokenService tokenService;

    //액세스 토큰 갱신 컨트롤러.
    //액세스 토큰이 만료되었을 때 리프레시 토큰을 사용하여 새로운 액세스 토큰 생성
    @PostMapping("/token")
    public ResponseEntity<?> createNewAccessToken(@RequestBody CreateAccessTokenRequest request){

        String refreshToken = request.getRefreshToken();

        //리프레시 토큰 누락 검사
        if(refreshToken==null || refreshToken.trim().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("리프레시 토큰이 없습니다.");
        }
        try {
            String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new CreateAccessTokenResponse(newAccessToken));
        }catch(TokenExpiredException e){
            //토큰 만료 -> 재로그인 필요.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new IllegalArgumentException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요."));
        }catch(InvalidTokenException e){
            //토큰이 유효하지 않음
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));
        }
    }
}
