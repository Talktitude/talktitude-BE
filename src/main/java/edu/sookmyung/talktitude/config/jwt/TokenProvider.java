package edu.sookmyung.talktitude.config.jwt;


import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.repository.ClientRepository;
import edu.sookmyung.talktitude.client.service.ClientService;
import edu.sookmyung.talktitude.member.model.BaseUser;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import edu.sookmyung.talktitude.member.service.MemberService;
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
    private final MemberRepository memberRepository;
    private final ClientRepository clientRepository;


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

    // 토큰으로부터 인증 객체 반환
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Long userId = claims.get("id", Long.class);
        String userType = claims.get("userType", String.class);

        // Member 조회
        if ("Member".equalsIgnoreCase(userType)) {
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
            Set<SimpleGrantedAuthority> authorities =
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
            return new UsernamePasswordAuthenticationToken(member, token, authorities);
        }else if ("Client".equalsIgnoreCase(userType)) {
            Client client = clientRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다."));
            Set<SimpleGrantedAuthority> authorities =
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_CLIENT"));
            return new UsernamePasswordAuthenticationToken(client, token, authorities); 
        }

        throw new RuntimeException("지원하지 않는 userType입니다: " + userType);
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserId(String token) {
        return getClaims(token).get("id", Long.class);
    }

    // 토큰에서 사용자 타입 추출
    public String getUserType(String token) {
        return getClaims(token).get("userType", String.class);
    }

    // 토큰에서 Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

    // 웹소켓 관련 코드
    public String getLoginId(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // subject = loginId
    }
}
