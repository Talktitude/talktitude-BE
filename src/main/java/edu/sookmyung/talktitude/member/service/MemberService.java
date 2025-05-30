package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.member.dto.MemberDto;
import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.model.RefreshToken;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public void register(MemberDto dto) {
        // 중복 체크
        if (memberRepository.findByLoginId(dto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다.");
        }

        // 회원 생성
        Member member = Member.builder()
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .isDeleted(false)
                .isFilter(true)
                .build();

        memberRepository.save(member);
    }


    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new IllegalStateException("찾을 수 없는 사용자입니다"));
    }

    public LoginResponse login(String loginId, String password) {

        //디버깅용
        System.out.println("현재 비밀번호 인코딩된 값"+passwordEncoder.encode(password));

        try {
            //AuthenticationManager 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginId, password)
            );

            //인증된 사용자 정보 가져오기
            Member member = (Member) authentication.getPrincipal();

            //토큰 생성
            String accessToken = tokenProvider.generateAccessToken(member);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(member);

            return new LoginResponse(accessToken, refreshToken.getRefreshToken());

        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        } catch (UsernameNotFoundException e) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        } catch (Exception e) {
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.");
        }
    }

 }
