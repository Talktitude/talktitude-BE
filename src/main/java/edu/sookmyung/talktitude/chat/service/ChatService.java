package edu.sookmyung.talktitude.chat.service;


import edu.sookmyung.talktitude.chat.dto.*;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.model.SenderType;
import edu.sookmyung.talktitude.chat.repository.ChatMessageRepository;
import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import edu.sookmyung.talktitude.chat.model.Status;
import edu.sookmyung.talktitude.client.model.*;
import edu.sookmyung.talktitude.client.repository.ClientRepository;
import edu.sookmyung.talktitude.client.repository.OrderMenuRepository;
import edu.sookmyung.talktitude.client.repository.OrderPaymentRepository;
import edu.sookmyung.talktitude.client.repository.OrderRepository;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import org.springframework.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ClientRepository clientRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅 세션 생성
    @Transactional
    public Long createChatSession(Client client, @Nullable CreateSessionRequest request) {
        Client persisted = clientRepository.findById(client.getId())
                .orElseThrow(() -> new BaseException(ErrorCode.CLIENT_NOT_FOUND));

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new BaseException(ErrorCode.ORDER_NOT_FOUND));
        }

        // TODO: 상담원 매칭 로직 → 지금은 임시로 첫 번째 상담원 지정
        Member agent = memberRepository.findAll().getFirst(); // 반드시 실제 상담원 존재해야 함

        ChatSession session = new ChatSession(
                null,
                agent,
                persisted,
                order,
                LocalDateTime.now(),
                Status.IN_PROGRESS // 기본값
        );

        chatSessionRepository.save(session);

        // 트랜잭션 커밋 후 상담원에게 "새 세션 생성" 이벤트 푸시
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            ChatSession finalSession = session;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    var push = new SessionCreatedPush(
                            finalSession.getId(),
                            finalSession.getClient().getLoginId(),
                            finalSession.getClient().getPhone(),
                            finalSession.getClient().getProfileImageUrl(),
                            finalSession.getStatus(),
                            finalSession.getCreatedAt() // 생성 직후엔 마지막 메시지 대신 생성 시각
                    );
                    String agentLoginId = finalSession.getMember().getLoginId();
                    // 상담원 전용 큐로 발송
                    messagingTemplate.convertAndSendToUser(agentLoginId, "/queue/sessions/created", push);
                }
            });
        }
        return session.getId();
    }

    // 상담원 상담 목록 조회 (상담원 ID 및 상태 필터)
    @Transactional
    public List<ChatSessionDto> getChatSessionsForMember(Long memberId, String statusStr) {
        List<ChatSession> sessions;

        if ("ALL".equalsIgnoreCase(statusStr)) {
            // 상태 필터 없이 전체 조회
            sessions = chatSessionRepository.findByMemberId(memberId);
        } else {
            // 문자열 -> Status enum 변환
            Status status;
            try {
                status = Status.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("status 값은 IN_PROGRESS 또는 FINISHED 여야 합니다.");
            }
            sessions = chatSessionRepository.findByMemberAndStatus(memberId, status);
        }

        return sessions.stream()
                .map(session -> {
                    LocalDateTime lastMessageTime = chatMessageRepository
                            .findTopByChatSessionOrderByCreatedAtDesc(session)
                            .map(ChatMessage::getCreatedAt)
                            .orElse(session.getCreatedAt());

                    return new ChatSessionDto(
                            session.getId(),
                            session.getClient().getLoginId(),
                            session.getClient().getPhone(),
                            session.getClient().getProfileImageUrl(),
                            session.getStatus(),
                            lastMessageTime
                    );
                })
                .sorted(Comparator.comparing(ChatSessionDto::getLastMessageTime).reversed())  // 최신순 정렬
                .toList();
    }

    // 상담 검색
    @Transactional(readOnly = true)
    public List<ChatSessionDto> searchByClientLoginId(Long memberId, String keyword, String statusStr) {
        String kw = keyword.trim();
        List<ChatSession> sessions;

        if ("ALL".equalsIgnoreCase(statusStr)) {
            sessions = chatSessionRepository
                    .findByMember_IdAndClient_LoginIdContainingIgnoreCase(memberId, kw);
        } else {
            Status status;
            try {
                status = Status.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BaseException(ErrorCode.CHATSESSION_INVALID_INPUT);
            }
            sessions = chatSessionRepository
                    .findByMember_IdAndStatusAndClient_LoginIdContainingIgnoreCase(memberId, status, kw);
        }

        return sessions.stream()
                .map(s -> {
                    var last = chatMessageRepository
                            .findTopByChatSessionOrderByCreatedAtDesc(s)
                            .map(ChatMessage::getCreatedAt)
                            .orElse(s.getCreatedAt());
                    return new ChatSessionDto(
                            s.getId(),
                            s.getClient().getLoginId(),
                            s.getClient().getPhone(),
                            s.getClient().getProfileImageUrl(),
                            s.getStatus(),
                            last
                    );
                })
                .sorted(Comparator.comparing(ChatSessionDto::getLastMessageTime).reversed())
                .toList();
    }

    // 채팅 세션 정보 조회
    @Transactional
    public ChatSessionDetailDto getChatSessionDetail(Long sessionId, Long memberId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅 세션이 존재하지 않습니다."));

        // 상담원이 이 세션의 상담원인지 검증
        if (!session.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("해당 채팅 세션에 접근 권한이 없습니다.");
        }

        Client client = session.getClient();
        return new ChatSessionDetailDto(session, client);
    }

    // 상담 종료
    @Transactional
    public void finishChatSession(Long sessionId, Long memberId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅 세션이 존재하지 않습니다."));

        if (!session.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("해당 채팅 세션을 종료할 권한이 없습니다.");
        }

        if (session.getStatus() == Status.FINISHED) {
            throw new BaseException(ErrorCode.INVALID_SESSION_STATE);
        }

        session.finish(); // 종료로 상태 변경

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            ChatSession finalSession = session;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    SessionStatusPush push = new SessionStatusPush(finalSession.getId(), finalSession.getStatus().name());
                    String agentLoginId  = finalSession.getMember().getLoginId();
                    String clientLoginId = finalSession.getClient().getLoginId();

                    // 같은 세션을 보고 있는 상담원에게 상태변경 알림
                    messagingTemplate.convertAndSendToUser(agentLoginId,  "/queue/chat/" + finalSession.getId() + "/status", push);
                    // 같은 세션을 보고 있는 고객에게도 상태변경 알림
                    messagingTemplate.convertAndSendToUser(clientLoginId, "/queue/chat/" + finalSession.getId() + "/status", push);
                }
            });
        }
    }
  
 
      public List<ChatMessage> findChatMessage(Long sessionId) {
        List<ChatMessage> chatMessage = chatMessageRepository.findByChatSessionId(sessionId);
        return chatMessage;
    }

    // WebSocket
    @Transactional
    public ChatMessage sendMessage(Long sessionId,
                                   SenderType senderType,
                                   String originalText,
                                   String convertedText) {
        // 1. 세션 확인
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅 세션이 존재하지 않습니다."));

        // 2. 종료된 세션이면 차단
        if (session.getStatus() == Status.FINISHED) {
            throw new IllegalStateException("종료된 세션입니다.");
        }

        // 3. 메시지 저장
        ChatMessage message = new ChatMessage(
                null,
                session,
                senderType,
                originalText,
                convertedText,
                LocalDateTime.now()
        );
        return chatMessageRepository.save(message);
    }

    // 채팅 내역 조회
    @Transactional(readOnly = true)
    public List<ChatMessage> findChatMessagesWithAccessCheck(Long sessionId, Long userId, String userType) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));

        boolean isAuthorized = false;
        if ("Member".equalsIgnoreCase(userType)) {
            isAuthorized = session.getMember().getId().equals(userId);
        } else if ("Client".equalsIgnoreCase(userType)) {
            isAuthorized = session.getClient().getId().equals(userId);
        }

        if (!isAuthorized) {
            throw new BaseException(ErrorCode.CHATSESSION_ACCESS_DENIED);
        }

        return chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId);
    }


    // 전체 주문 목록 조회
    @Transactional(readOnly = true)
    public List<OrderHistory> getOrderHistory(Client client) {
        List<Order> orderList = orderRepository.findByClientLoginId(client.getLoginId());
        return orderList.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .map(order -> {
                    List<OrderMenu> orderMenus = order.getOrderMenus();
                    OrderPayment payment = order.getOrderPayment();
                    //안전성 체크
                    if (orderMenus.isEmpty()) {
                        throw new BaseException(ErrorCode.ORDER_MENU_NOT_FOUND);
                    }
                    if (payment == null) {
                        throw new BaseException(ErrorCode.ORDER_PAYMENT_NOT_FOUND);
                    }

                    String mainMenu = orderMenus.getFirst().getMenu();
                    int othersCount = Math.max(0, orderMenus.size() - 1);

                    int totalPrice =payment.getPaidAmount();
                    NumberFormat formatter = NumberFormat.getInstance(); //3자리마다 쉼표 추가
                    String formattedPrice = formatter.format(totalPrice);


                    String orderSummary = (othersCount > 0)
                            ? mainMenu + " 외 " + othersCount + "개 " + formattedPrice + "원"
                            : mainMenu + " " + formattedPrice + "원";

                    return new OrderHistory(
                        order.getId(),
                        order.getRestaurant().getImageUrl(),
                        order.getRestaurant().getName(), orderSummary, order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm"))
                );
                })
                .collect(Collectors.toList());
    }

    /**
     * 고객용 상담 목록 조회
     */

    @Transactional(readOnly = true)
    public ClientChatSessionListResponse getClientSessionLists(Long clientId) {

        List<ChatSession> inProgEntities =
                chatSessionRepository.findByClient_IdAndStatus(clientId, Status.IN_PROGRESS);
        List<ChatSession> finishedEntities =
                chatSessionRepository.findByClient_IdAndStatus(clientId, Status.FINISHED);

        List<ClientChatSessionDto> inProgress = inProgEntities.stream()
                .map(this::toClientItem)
                .toList();

        List<ClientChatSessionDto> finished = finishedEntities.stream()
                .map(this::toClientItem)
                .toList();

        return new ClientChatSessionListResponse(
                inProgress.size(),
                finished.size(),
                inProgress,
                finished
        );
    }

    // ChatSession → 고객 리스트 아이템 DTO
    private ClientChatSessionDto toClientItem(ChatSession cs) {
        // 1. 마지막 메시지(클라이언트 관점 textToShow)
        String lastMessage = chatMessageRepository
                .findTopByChatSessionOrderByCreatedAtDesc(cs)
                .map(m -> (m.getSenderType() == SenderType.CLIENT)
                        ? (m.getConvertedText() != null ? m.getConvertedText() : m.getOriginalText())
                        : m.getOriginalText())
                .orElse("대화가 시작되었습니다.");

        // 2. 가게/주문 요약/총액
        String storeName = null;
        String storeImageUrl = null;
        String orderSummary = "주문 외 문의";

        if (cs.getOrder() != null) {
            Order order = cs.getOrder();

            // 가게 정보
            Restaurant r = order.getRestaurant();
            if (r != null) {
                storeName = r.getName();
                storeImageUrl = r.getImageUrl();
            }

            // 주문 메뉴 한 줄 요약
            List<OrderMenu> menus = orderMenuRepository.findByOrderId(order.getId());
            String summaryCore;
            if (menus != null && !menus.isEmpty()) {
                String first = menus.get(0).getMenu();
                int others = Math.max(0, menus.size() - 1);
                summaryCore = (others > 0) ? first + " 외 " + others + "개" : first;
            } else {
                summaryCore = "주문 외 문의";
            }

            // 결제 금액
            Integer paidAmount = orderPaymentRepository.findPaidAmountByOrderId(order.getId()).orElse(null);

            if (paidAmount != null) { // 결제 정보가 있을 때
                String won = String.format("%,d원", paidAmount);
                orderSummary = summaryCore + " " + won; // 0원이든 아니든 금액 붙임
            } else {
                orderSummary = summaryCore; // 결제 정보 자체가 없을 때
            }
        }

        return new ClientChatSessionDto(
                cs.getId(),
                cs.getStatus().name(),
                storeName,
                storeImageUrl,
                orderSummary,
                lastMessage
        );
    }

    // 고객 - 채팅방 상단 헤더
    @Transactional(readOnly = true)
    public ClientChatRoomHeader getClientChatRoomHeader(Long sessionId, Long clientId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));

        // 권한 체크: 이 세션의 고객 본인만 접근 가능
        if (!session.getClient().getId().equals(clientId)) {
            throw new BaseException(ErrorCode.REPORT_ACCESS_DENIED);
        }

        // 기본 값: 주문 외 문의
        Long orderId = null;
        String title = "주문 외 문의";
        boolean orderLinked = false;

        if (session.getOrder() != null) {
            orderLinked = true;
            orderId = session.getOrder().getId();
            title = session.getOrder().getRestaurant().getName();
        }

        return new ClientChatRoomHeader(
                session.getId(),
                orderId,
                title,
                orderLinked,
                session.getStatus()
        );
    }

    //최근 문장 조회
    @Transactional(readOnly = true)
    public List<ChatMessage> getRecentMessages(Long sessionId,int count){
        return chatMessageRepository.findRecentByChatSessionId(sessionId,count);
    }
}
