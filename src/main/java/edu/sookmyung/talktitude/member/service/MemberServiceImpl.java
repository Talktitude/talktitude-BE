package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.member.config.TokenProvider;
import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.model.RefreshToken;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.HOURS;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;

    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new IllegalStateException("Member not found"));
    }

    public LoginResponse login(String loginId, String password) {

        System.out.println("여기이이ㅣ잉");
        System.out.println("여기 바뀐 토큰"+bCryptPasswordEncoder.encode(password));
        //AuthenticationManager 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginId, password)
        );

        //인증된 사용자 정보 가져오기
        Member member = (Member) authentication.getPrincipal();

        //토큰 생성
        String accessToken = tokenProvider.generateAccessToken(member);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(member);

        return new LoginResponse(accessToken,refreshToken.getRefreshToken());


    }

 }
