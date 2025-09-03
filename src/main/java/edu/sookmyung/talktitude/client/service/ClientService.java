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
import edu.sookmyung.talktitude.memo.dto.MemoResponse;
import edu.sookmyung.talktitude.memo.repository.MemoRepository;
import edu.sookmyung.talktitude.report.dto.ReportDetailByClient;
import edu.sookmyung.talktitude.report.dto.ReportListByClient;
import edu.sookmyung.talktitude.report.model.MemoPhase;
import edu.sookmyung.talktitude.report.model.Report;
import edu.sookmyung.talktitude.report.repository.ReportRepository;
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
    private final ChatSessionRepository chatSessionRepository;
    private final ReportRepository reportRepository;
    private final MemoRepository memoRepository;



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
            log.error("아이디 또는 비밀번호가 올바르지 않습니다.:{}",e.getMessage());
            throw new BaseException(ErrorCode.WRONG_CREDENTIALS);
        } catch (UsernameNotFoundException e) {
            log.error("존재하지 않는 사용자 입니다:{}", e.getMessage());
            throw new  BaseException(ErrorCode.WRONG_CREDENTIALS);
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생:{}", e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
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

    //오른쪽 정보 패널 - 주문 정보 조회 메서드(채팅에 참가한 고객용)
    @Transactional(readOnly = true)
    public List<OrderInfo> getOrderById(Long sessionId, Member member) {

        Client client = validateAndGetClient(sessionId, member);

        //고객의 주문 내역을 전부 조회
        List<Order> orders = orderRepository.findByClientLoginId(client.getLoginId());

        // 배달 정보와 함께 orderInfo dto 구성
        return orders.stream()
                .map(order->{
                    OrderDelivery orderDelivery = order.getOrderDelivery();

                    if(orderDelivery==null){
                        throw new BaseException(ErrorCode.ORDER_DELIVERY_NOT_FOUND);
                    }
                    return OrderInfo.convertToOrderInfo(order, orderDelivery);
                }).collect(Collectors.toList());
    }


    //오른쪽 정보 패널 - 주문 상세 조회 메서드
    @Transactional(readOnly = true)
    public OrderDetailInfo getOrderDetailById(String orderNumber, Member member, Long sessionId) {

        Client client = validateAndGetClient(sessionId, member);

        //특정 주문 내역의 상세 주문 정보 조회
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BaseException(ErrorCode.ORDER_NOT_FOUND));

        //해당 주문이 현재 채팅 세션의 클라이언트 소유인지 검증
        if(!order.getClient().getLoginId().equals(client.getLoginId())) {
            throw new BaseException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        OrderDelivery orderDelivery = order.getOrderDelivery();
        if(orderDelivery==null){
            throw new BaseException(ErrorCode.ORDER_DELIVERY_NOT_FOUND);
        }

        OrderPayment orderPayment = order.getOrderPayment();
        if(orderPayment==null){
            throw new BaseException(ErrorCode.ORDER_PAYMENT_NOT_FOUND);
        }

        List<OrderMenu> orderMenus = order.getOrderMenus();
        if(orderMenus.isEmpty()){
            throw new BaseException(ErrorCode.ORDER_MENU_NOT_FOUND);
        }

        return OrderDetailInfo.convertToOrderDetailInfo(order, orderDelivery, orderPayment, orderMenus);
    }

    // 오른쪽 정보 패널 -> 고객별 상담 목록 조회
    @Transactional(readOnly = true)
    public List<ReportListByClient> getReportsByClient(Long sessionId, Member member) {
        Client client = validateAndGetClient(sessionId, member);
        return reportRepository.findByClientLoginId(client.getLoginId())
                .stream()
                .map(ReportListByClient::convertToReportListByClient)
                .collect(Collectors.toList());

    }

    // 오른쪽 정보 패널 -> 고객별 상담 상세 조회
    @Transactional(readOnly = true)
    public ReportDetailByClient getReportDetailByClient(Long reportId,Long sessionId, Member member) {

        Client client = validateAndGetClient(sessionId, member);
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND));
        //해당 리포트가 이 고객의 것인지 검증
        if(!report.getChatSession().getClient().getLoginId().equals(client.getLoginId())) {
            throw new BaseException(ErrorCode.REPORT_ACCESS_DENIED);
        }
        return ReportDetailByClient.convertToReportDetailByClient(report);

    }

    //오른쪽 정보 패널 -> 상담 중에 작성된 메모 조회
    @Transactional(readOnly = true)
    public List<MemoResponse> getDuringChatUserMemos(Long sessionId, Member currentMember) {

        ChatSession chatSession = validateAndGetChatSession( sessionId, currentMember);
        //현재 로그인 사용자 + 상담 중 작성된 메모 기준으로 조회
        return memoRepository.findByChatSessionAndMemberAndMemoPhase(chatSession,currentMember, MemoPhase.DURING_CHAT)
                .stream()
                .map(MemoResponse::convertToMemoResponse)
                .collect(Collectors.toList());
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

    //검증 이후 ChatSession을 반환하는 메서드
    private ChatSession validateAndGetChatSession(Long sessionId, Member member) {
        ChatSession chatSession = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));

        if (!isChatSessionParticipant(member, chatSession)) {
            throw new BaseException(ErrorCode.CHAT_SESSION_ACCESS_DENIED);
        }

        return chatSession;
    }
}
