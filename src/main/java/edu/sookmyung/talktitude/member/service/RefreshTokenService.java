package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.model.RefreshToken;

public interface RefreshTokenService {
    public RefreshToken findByRefreshToken(String refreshToken);
    public RefreshToken createRefreshToken(Member member);
}
