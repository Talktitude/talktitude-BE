package edu.sookmyung.talktitude.client.service;

import edu.sookmyung.talktitude.client.dto.ClientDto;
import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.repository.ClientRepository;
import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.model.RefreshToken;
import edu.sookmyung.talktitude.member.service.RefreshTokenService;
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

@Service
@RequiredArgsConstructor
public class ClientService {

    @Autowired
    @Qualifier("clientAuthManager")
    private AuthenticationManager clientAuthManager;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public void register(ClientDto dto) {
        // 중복 로그인 ID 검사
        if (clientRepository.findByLoginId(dto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다.");
        }

        // Client 엔티티 생성
        Client client = Client.builder()
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .isDeleted(false)
                .build();

        clientRepository.save(client);
    }

    public Client findClientById(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new IllegalStateException("찾을 수 없는 사용자입니다"));
    }

    public LoginResponse login(String loginId, String password) {

        try {
            //AuthenticationManager 인증 처리
            Authentication authentication = clientAuthManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginId, password)
            );

            //인증된 사용자 정보 가져오기
            Client client = (Client) authentication.getPrincipal();

            //토큰 생성
            String accessToken = tokenProvider.generateAccessToken(client);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(client);

            return new LoginResponse(accessToken, refreshToken.getRefreshToken());

        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        } catch (UsernameNotFoundException e) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        } catch (Exception e) {
            throw new RuntimeException("로그인 처리 중 오류가 발생했습니다."+e);
        }
    }
}
