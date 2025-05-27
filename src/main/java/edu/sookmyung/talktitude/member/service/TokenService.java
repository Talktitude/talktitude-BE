package edu.sookmyung.talktitude.member.service;

public interface TokenService {
    public String createNewAccessToken(String refreshToken);
}
