package edu.sookmyung.talktitude.config.jwt;

import edu.sookmyung.talktitude.member.model.BaseUser;
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
    public String generateAccessToken(BaseUser baseUser) {
        Date now = new Date();
        return makeToken(new Date(now.getTime()+Duration.ofHours(2).toMillis()),baseUser,"ACCESS");
    }

    //리프레쉬 토큰 생성 메서드
    public String generateRefreshToken(BaseUser baseUser) {
        Date now = new Date();
        return makeToken(new Date(now.getTime()+Duration.ofDays(14).toMillis()),baseUser,"REFRESH");
    }

    //jwt 토큰 생성 메서드
    private String makeToken(Date expiry, BaseUser baseUser, String tokenType){
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(baseUser.getUsername())
                .claim("id",baseUser.getId())
                .claim("type",tokenType)
                .claim("userType",baseUser.getUserType())
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
    public Long getUserId(String token){
        Claims claims = getClaims(token);

        return claims.get("id",Long.class);
    }

    // 토큰 타입을 가져오는 메서드
    public String getUserType(String token){
        Claims claims = getClaims(token);
        return claims.get("userType",String.class);
    }
    //token에서 클레임 정보 추출 메서드
    private Claims getClaims(String token){
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }
}
