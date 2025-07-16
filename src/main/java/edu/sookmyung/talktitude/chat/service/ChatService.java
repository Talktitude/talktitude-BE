package edu.sookmyung.talktitude.chat.service;

import edu.sookmyung.talktitude.chat.dto.ChatSessionDto;
import edu.sookmyung.talktitude.chat.dto.CreateSessionRequest;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.repository.ChatMessageRepository;
import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import edu.sookmyung.talktitude.chat.model.Status;
import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.client.repository.ClientRepository;
import edu.sookmyung.talktitude.client.repository.OrderRepository;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ClientRepository clientRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Long createChatSession(CreateSessionRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        Order order = null;
        if (request.getOrderID() != null) {
            order = orderRepository.findById(request.getOrderID())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
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
        Status status = statusStr.equalsIgnoreCase("ALL") ? null : Status.valueOf(statusStr);
        List<ChatSession> sessions = chatSessionRepository.findByMemberAndStatus(memberId, status);

        return sessions.stream().map(session -> {
            // 해당 세션의 마지막 메시지 시간 조회
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
        }).toList();
    }
}

