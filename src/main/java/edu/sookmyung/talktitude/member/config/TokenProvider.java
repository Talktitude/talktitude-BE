package edu.sookmyung.talktitude.member.config;

import edu.sookmyung.talktitude.member.model.Member;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Header;


import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;

    public String generateToken(Member member, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime()+expiredAt.toMillis()),member);
    }

    //jwt 토큰 생성 메서드
    private String makeToken(Date expiry, Member member){
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(member.getUsername())
                .claim("id",member.getId())
                .signWith(SignatureAlgorithm.HS256,jwtProperties.getSecretKey())
                .compact();
    }

    //jwt 유효성 검증 메소드
    public boolean validToken(String token){
        try{
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey()) //비밀값으로 복호화
                    .parseClaimsJws(token);
            return true;
        }catch(Exception e){
            return false; //복호화 과정에서 에러가 나면 유효하지 않은 토큰
        }
    }

    //토큰 기반으로 인증 정보를 가져오는 메소드
    public Authentication getAuthentication(String token){
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        //토큰 기반으로 인증 정보 생성.
        return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(claims.getSubject(),"",authorities),token,authorities);
    }

    //토큰 기반으로 유저 ID 가져오는 메소드
    public Long getMemberId(String token){
        Claims claims = getClaims(token);

        return claims.get("id",Long.class);
    }

    private Claims getClaims(String token){
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }
}
