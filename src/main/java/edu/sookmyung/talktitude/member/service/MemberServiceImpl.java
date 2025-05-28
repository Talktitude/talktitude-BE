package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.member.config.TokenProvider;
import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.model.RefreshToken;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new IllegalStateException("Member not found"));
    }

    public LoginResponse login(String loginId, String password) {

        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new IllegalStateException("Member not found"));

        System.out.println("==================== 디버깅 시작 ====================");
        System.out.println("1. 입력받은 loginId: '" + loginId + "'");
        System.out.println("2. 입력받은 password: '" + password + "'");
        System.out.println("3. password 길이: " + password.length());

        String dbHash = member.getPassword();
        System.out.println("4. DB에서 가져온 해시: '" + dbHash + "'");
        System.out.println("5. DB 해시 길이: " + dbHash.length());

        // 🔥 가장 중요한 테스트: BCrypt 자체 동작 확인
        String newHash = bCryptPasswordEncoder.encode(password);
        System.out.println("6. 방금 생성한 새 해시: '" + newHash + "'");
        System.out.println("7. 새 해시 길이: " + newHash.length());

        boolean immediateTest = bCryptPasswordEncoder.matches(password, newHash);
        System.out.println("8. 🔥 새 해시 즉시 검증 결과: " + immediateTest);

        if (!immediateTest) {
            System.out.println("🚨🚨🚨 BCrypt 자체에 심각한 문제가 있습니다!");
            throw new RuntimeException("BCrypt 오류");
        }

        // DB 해시로 검증
        boolean dbHashTest = bCryptPasswordEncoder.matches(password, dbHash);
        System.out.println("9. DB 해시 검증 결과: " + dbHashTest);

        // 해시 비교 (참고용)
        System.out.println("10. 두 해시가 같은가?: " + newHash.equals(dbHash));

        if (!dbHashTest) {
            System.out.println("11. 🔍 DB 해시 문제 상세 분석:");
            System.out.println("    - 새 해시와 DB 해시 길이 차이: " + (newHash.length() - dbHash.length()));

            // 바이트 레벨 비교
            byte[] newHashBytes = newHash.getBytes();
            byte[] dbHashBytes = dbHash.getBytes();
            System.out.println("    - 새 해시 바이트 수: " + newHashBytes.length);
            System.out.println("    - DB 해시 바이트 수: " + dbHashBytes.length);

            throw new IllegalArgumentException("Invalid Password");
        }

        System.out.println("==================== 디버깅 완료 ====================");        //비밀번호 검증
        if(!bCryptPasswordEncoder.matches(password,member.getPassword())){
            throw new IllegalArgumentException("Invalid Password");
        }
        //토큰 생성
        String accessToken = tokenProvider.generateToken(member, Duration.of(2,HOURS));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(member);

        return new LoginResponse(accessToken, refreshToken.getRefreshToken());

    }

 }
