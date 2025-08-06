package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.member.dto.MemberDto;
import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.token.model.RefreshToken;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import edu.sookmyung.talktitude.token.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    @Qualifier("memberAuthManager")
    private final AuthenticationManager memberAuthManager;

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    public void register(MemberDto dto) {
        // 중복 체크
        boolean exists = memberRepository.findByLoginId(dto.getLoginId()).isPresent();
        if (exists) {
            throw new BaseException(ErrorCode.DUPLICATE_LOGIN_ID);
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

    public boolean isDuplicateLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId).isPresent();
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public LoginResponse login(String loginId, String password) {

        try {
            //AuthenticationManager 인증 처리
            Authentication authentication = memberAuthManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginId, password)
            );

            //인증된 사용자 정보 가져오기
            Member member = (Member) authentication.getPrincipal();

            //토큰 생성
            String accessToken = tokenProvider.generateAccessToken(member);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(member);

            return new LoginResponse(accessToken, refreshToken.getRefreshToken());

        } catch (BadCredentialsException e) {
            throw new BaseException(ErrorCode.WRONG_CREDENTIALS);
        } catch (UsernameNotFoundException e) {
            //throw new BaseException(ErrorCode.AUTHENTICATION_FAILED);
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        } catch (Exception e) {
            e.printStackTrace();  // 로그 확인용
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다.");
        }
    }
 }
