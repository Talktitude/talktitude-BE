package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.exception.InvalidTokenException;
import edu.sookmyung.talktitude.exception.TokenExpiredException;
import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import edu.sookmyung.talktitude.member.model.Member;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;

    public String createNewAccessToken(String refreshToken) {
        try {
            //리프레시 토큰이 유효성 검사에 실패하면 예외 발생
            tokenProvider.validToken(refreshToken);

            Long memberId = tokenProvider.getMemberId(refreshToken);
            Member member = memberService.findMemberById(memberId);

            return tokenProvider.generateAccessToken(member);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired");
            //토큰이 invalid할 경우
        } catch (UnsupportedJwtException | MalformedJwtException |
                 SignatureException | IllegalArgumentException e) {
            throw new InvalidTokenException("Token is invalid");
        }
    }
}
