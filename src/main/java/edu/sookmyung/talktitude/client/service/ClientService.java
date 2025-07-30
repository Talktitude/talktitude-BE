package edu.sookmyung.talktitude.client.service;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import edu.sookmyung.talktitude.client.dto.ClientDto;
import edu.sookmyung.talktitude.client.dto.ClientInfo;
import edu.sookmyung.talktitude.client.dto.OrderDetailInfo;
import edu.sookmyung.talktitude.client.dto.OrderInfo;
import edu.sookmyung.talktitude.client.model.*;
import edu.sookmyung.talktitude.client.repository.*;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.config.jwt.TokenProvider;
import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.token.model.RefreshToken;
import edu.sookmyung.talktitude.token.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
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
    private final OrderRepository orderRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final ChatSessionRepository chatSessionRepository;



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


    public Client findClientById(Long id) {
        return clientRepository.findById(id).orElseThrow(() -> new BaseException(ErrorCode.CLIENT_NOT_FOUND));
    }


    //오른쪽 정보 패널 - 사용자 정보 조회 메서드
    public ClientInfo getClientInfoById(Long sessionId, Member member) {
        Client client = validateAndGetClient(sessionId, member);
        return ClientInfo.convertToClientInfo(client);
    }


    // 채팅 세션에 참가한 회원인지 검증
    public boolean isChatSessionParticipant(Member member, ChatSession chatSession) {
        log.info("현재 로그인한 id:{}, 채팅 세션에 참가한 id:{}",member.getId(),chatSession.getMember().getId());
        return Objects.equals(member.getId(), chatSession.getMember().getId());
    }

    //검증 이후 Client를 반환하는 메서드
    private Client validateAndGetClient(Long sessionId, Member member) {
        ChatSession chatSession = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));

        if(!isChatSessionParticipant(member, chatSession)){
            throw new BaseException(ErrorCode.CHAT_SESSION_ACCESS_DENIED);
        }

        return clientRepository.findById(chatSession.getClient().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLIENT_NOT_FOUND));
    }


}
