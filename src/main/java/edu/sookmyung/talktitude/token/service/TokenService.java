package edu.sookmyung.talktitude.token.service;

import edu.sookmyung.talktitude.client.service.ClientService;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import edu.sookmyung.talktitude.member.model.BaseUser;
import edu.sookmyung.talktitude.member.service.MemberService;
import edu.sookmyung.talktitude.token.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;
    private final ClientService clientService;
    private final RefreshTokenRepository refreshTokenRepository;

    public String createNewAccessToken(String refreshToken) {
        try {
            //리프레시 토큰이 유효성 검사에 실패하면 예외 발생
            tokenProvider.validToken(refreshToken);

            // JWT에서 사용자 타입과 ID 추출
            String userType = tokenProvider.getUserType(refreshToken);
            Long userId = tokenProvider.getUserId(refreshToken);

            BaseUser user = findUserByType(userType, userId);
            return tokenProvider.generateAccessToken(user);

        } catch (ExpiredJwtException e) {
            throw new BaseException(ErrorCode.EXPIRED_TOKEN);
            //토큰이 invalid할 경우
        } catch (UnsupportedJwtException | MalformedJwtException |
                 SignatureException | IllegalArgumentException e) {
            throw new BaseException(ErrorCode.INVALID_TOKEN);
        }
    }

    private BaseUser findUserByType(String userType, Long userId) {
        return switch(userType){
            case "Member" -> memberService.findMemberById(userId);
            case "Client"-> clientService.findClientById(userId);
            default -> throw new BaseException(ErrorCode.WRONG_USERTYPE);
        };
    }

    // 로그아웃 (사용자 ID & 타입으로 Refresh Token 삭제)
    @Transactional
    public void deleteRefreshToken(Long userId, String userType) {
        refreshTokenRepository.findByUserIdAndUserType(userId,userType)
                .ifPresent(refreshTokenRepository::delete);
    }
}
