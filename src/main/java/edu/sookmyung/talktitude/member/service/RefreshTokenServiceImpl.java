package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.member.config.TokenProvider;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.model.RefreshToken;
import edu.sookmyung.talktitude.member.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(()-> new IllegalArgumentException(("Unexpected token")));
    }

    public RefreshToken createRefreshToken(Member member){

        //기존 리프레시 토큰이 있다면 삭제
        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresent(token -> refreshTokenRepository.deleteByMemberId(member.getId()));

        String refreshTokenValue = tokenProvider.generateToken(member, Duration.ofHours(7));

        RefreshToken refreshToken = new RefreshToken(member.getId(),refreshTokenValue);

        return refreshTokenRepository.save(refreshToken);
    }
}
