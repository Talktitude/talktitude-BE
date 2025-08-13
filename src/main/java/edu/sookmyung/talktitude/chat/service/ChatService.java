package edu.sookmyung.talktitude.chat.service;

import edu.sookmyung.talktitude.chat.dto.ChatSessionDetailDto;
import edu.sookmyung.talktitude.chat.dto.ChatSessionDto;
import edu.sookmyung.talktitude.chat.dto.CreateSessionRequest;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.model.SenderType;
import edu.sookmyung.talktitude.chat.repository.ChatMessageRepository;
import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import edu.sookmyung.talktitude.chat.model.Status;
import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.client.repository.ClientRepository;
import edu.sookmyung.talktitude.client.repository.OrderRepository;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ClientRepository clientRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    // 채팅 세션 생성
    @Transactional
    public Long createChatSession(CreateSessionRequest request) {
        Client client = clientRepository.findById(request.getClientId())
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
                client,
                order,
                LocalDateTime.now(),
                Status.IN_PROGRESS // 기본값
        );

        chatSessionRepository.save(session);
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
                            null, // profileImageUrl, 이후 구현 예정
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
            throw new BaseException(ErrorCode.INVALID_SESSION_STATE); // ✅ 새로 추가
        }

        session.finish(); // 종료로 상태 변경
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

}
