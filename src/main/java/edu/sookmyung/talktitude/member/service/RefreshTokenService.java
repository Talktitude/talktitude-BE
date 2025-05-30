package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.model.RefreshToken;
import edu.sookmyung.talktitude.member.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(()-> new IllegalArgumentException(("Unexpected token")));
    }

    public RefreshToken createRefreshToken(Member member){
        //기존 리프레시 토큰 찾기
        RefreshToken refreshToken = (RefreshToken) refreshTokenRepository.findByMemberId(member.getId())
                .orElse(new RefreshToken(member.getId(),null));

        String newToken = tokenProvider.generateRefreshToken(member);
        refreshToken.update(newToken);

        return refreshTokenRepository.save(refreshToken);
    }
}
