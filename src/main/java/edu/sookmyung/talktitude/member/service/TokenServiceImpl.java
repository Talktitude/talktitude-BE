package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.member.config.TokenProvider;
import edu.sookmyung.talktitude.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;

    public String createNewAccessToken(String refreshToken) {
        //토큰 유효성 검사에 실패하면 예외 발생
        if(!tokenProvider.validToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        Long memberId = tokenProvider.getMemberId(refreshToken);
        Member member = memberService.findMemberById(memberId);

        return tokenProvider.generateToken(member, Duration.ofHours(2)); //유효 시간 논의하기
    }
}
