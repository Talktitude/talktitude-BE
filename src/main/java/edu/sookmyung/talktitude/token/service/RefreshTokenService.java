package edu.sookmyung.talktitude.token.service;

import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import edu.sookmyung.talktitude.member.model.BaseUser;
import edu.sookmyung.talktitude.token.model.RefreshToken;
import edu.sookmyung.talktitude.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;


    public RefreshToken createRefreshToken(BaseUser baseUser){
        //기존 리프레시 토큰 찾기
        RefreshToken refreshToken = refreshTokenRepository.findByMemberIdAndUserType(baseUser.getId(),baseUser.getUserType())
                .orElse(new RefreshToken(baseUser.getId(),null,baseUser.getUserType()));

        String newToken = tokenProvider.generateRefreshToken(baseUser);
        refreshToken.update(newToken);

        return refreshTokenRepository.save(refreshToken);
    }
}
