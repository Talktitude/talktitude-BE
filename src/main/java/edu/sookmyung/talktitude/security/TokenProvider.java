package edu.sookmyung.talktitude.security;

import edu.sookmyung.talktitude.member.model.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;

    //액세스 토큰 생성 메서드
    public String generateAccessToken(Member member) {
        Date now = new Date();
        return makeToken(new Date(now.getTime()+Duration.ofHours(2).toMillis()),member,"ACCESS");
    }

    //리프레쉬 토큰 생성 메서드
    public String generateRefreshToken(Member member) {
        Date now = new Date();
        return makeToken(new Date(now.getTime()+Duration.ofDays(14).toMillis()),member,"REFRESH");
    }

    //jwt 토큰 생성 메서드
    private String makeToken(Date expiry, Member member, String tokenType){
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(member.getUsername())
                .claim("id",member.getId())
                .claim("type",tokenType)
                .signWith(SignatureAlgorithm.HS256,jwtProperties.getSecretKey())
                .compact();
    }

    //jwt 유효성 검증 메소드
    public boolean validToken(String token){
        try{
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //토큰 기반으로 인증 정보를 가져오는 메소드
    public Authentication getAuthentication(String token){
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        //토큰 기반으로 인증 정보(Authentication 객체) 생성.
        return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(claims.getSubject(),"",authorities),token,authorities);
    }

    //토큰 기반으로 유저 ID 가져오는 메소드
    public Long getMemberId(String token){
        Claims claims = getClaims(token);

        return claims.get("id",Long.class);
    }

    //token에서 클레임 정보 추출 메서드
    private Claims getClaims(String token){
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }
}
