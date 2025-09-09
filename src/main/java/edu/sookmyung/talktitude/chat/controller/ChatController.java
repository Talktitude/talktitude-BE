package edu.sookmyung.talktitude.chat.controller;

import edu.sookmyung.talktitude.chat.dto.*;
import edu.sookmyung.talktitude.chat.dto.recommend.CreateMessageRequest;
import edu.sookmyung.talktitude.chat.dto.recommend.RecommendListDto;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.service.ChatService;
import edu.sookmyung.talktitude.chat.service.RecommendService;
import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.common.response.ApiResponse;
import edu.sookmyung.talktitude.member.model.BaseUser;
import edu.sookmyung.talktitude.member.model.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final RecommendService recommendService;

    public ChatController(ChatService chatService, RecommendService recommendService) {
        this.chatService = chatService;
        this.recommendService = recommendService;
    }

    // 고객 - 상담 세션 생성
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createSession(
            @AuthenticationPrincipal Client client,
            @RequestBody(required = false) CreateSessionRequest request
    ) {
        Long sessionId = chatService.createChatSession(client, request);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("sessionId", sessionId), "상담 세션이 생성되었습니다."));
    }

    // 고객 - 상담 전 주문 목록 조회
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderHistory>>> getOrderHistory(@AuthenticationPrincipal Client client){
        List<OrderHistory> orderHistoryList = chatService.getOrderHistory(client);
        return ResponseEntity.ok(ApiResponse.ok(orderHistoryList));
    }

    // 상담원 - 상담 목록 조회
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatSessionDto>>> getSessions(
            @AuthenticationPrincipal Member member,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        List<ChatSessionDto> result = chatService.getChatSessionsForMember(member.getId(), status);
        return ResponseEntity.ok(ApiResponse.ok(result, "상담 목록 조회 성공"));
    }

    // 상담원 - 채팅 세션 정보 조회
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ChatSessionDetailDto>> getChatSessionDetail(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Member member
    ) {
        ChatSessionDetailDto session = chatService.getChatSessionDetail(sessionId, member.getId());
        return ResponseEntity.ok(ApiResponse.ok(session, "상담 세션 상세 조회 성공"));
    }

    // 상담원 - 상담 종료
    @PatchMapping("/sessions/{sessionId}/finish")
    public ResponseEntity<ApiResponse<Void>> finishSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Member member
    ) {
        chatService.finishChatSession(sessionId, member.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "상담이 정상적으로 종료되었습니다."));
    }

    // 상담원, 고객 - 채팅 내역 조회
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal BaseUser user
    ) {

        List<ChatMessage> messages =
                chatService.findChatMessagesWithAccessCheck(sessionId, user.getId(), user.getUserType());

        List<ChatMessageResponse> response = messages.stream()
                .map(m -> new ChatMessageResponse(m, user.getUserType()))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(response, "채팅 메시지 조회 성공"));
    }

    // 상담원 - 상담 검색(고객 loginId + 상태 필터(ALL/IN_PROGRESS/FINISHED)
    @GetMapping("/sessions/search")
    public ResponseEntity<ApiResponse<List<ChatSessionDto>>> search(
            @AuthenticationPrincipal Member member,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        List<ChatSessionDto> list =
                chatService.searchByClientLoginId(member.getId(), keyword, status);
        return ResponseEntity.ok(ApiResponse.ok(list, "채팅방 검색 성공"));
    }


    // 상담원 - 상담 목록 조회
    @GetMapping("/client/sessions")
    public ResponseEntity<ApiResponse<ClientChatSessionListResponse>> getClientSessionsOverview(
            @AuthenticationPrincipal Client client
    ) {
        ClientChatSessionListResponse data =
                chatService.getClientSessionLists(client.getId());
        return ResponseEntity.ok(ApiResponse.ok(data, "상담 목록 조회 성공"));
    }

    // 상담원/고객 - 특정 메시지의 추천답변 조회(없으면 생성→반환)
    @GetMapping("/sessions/{sessionId}/messages/{messageId}/recommendations")
    public ResponseEntity<ApiResponse<RecommendListDto>> getOrGenerateRecommendations(
            @PathVariable Long sessionId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal BaseUser user
    ) {
        // 접근권한은 기존 getMessages와 동일하게 검사되어야 함(생략 시 service에서 검사 추가)
        RecommendListDto data = recommendService.generate(messageId);
        return ResponseEntity.ok(ApiResponse.ok(data, "추천 답변 생성/조회 성공"));
    }



    // 추천 답변 test용 엔드포인트
    // 메시지를 저장하고, 그 메시지에 대한 추천답변을 즉시 생성/반환 (웹소켓 불필요)
    @PostMapping("/sessions/{sessionId}/messages/test-recommendations")
    public ResponseEntity<ApiResponse<RecommendListDto>> createMessageAndRecommend(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal BaseUser user,
            @RequestParam(defaultValue = "false") boolean async,   // ← 추가
            @RequestBody CreateMessageRequest req
    ) {
        // 접근 권한 확인
        chatService.findChatMessagesWithAccessCheck(sessionId, user.getId(), user.getUserType());

        // 메시지 저장
        ChatMessage m = chatService.sendMessage(
                sessionId,
                req.getSenderType(),
                req.getOriginalText(),
                null
        );

        if (async) {
            // 비동기 생성 → WebSocket으로 푸시
            recommendService.generateAndPush(m.getId());
            return ResponseEntity.ok(ApiResponse.ok(
                    RecommendListDto.builder().messageId(m.getId()).items(List.of()).build(),
                    "추천 생성 중(완료 시 WebSocket으로 푸시됨)")
            );
        } else {
            // 동기 생성(POSTMAN 테스트용)
            RecommendListDto data = recommendService.generate(m.getId());
            return ResponseEntity.ok(ApiResponse.ok(data, "추천 답변 생성/조회 성공"));
        }
    }

    // 고객 - 채팅방 상단 헤더
    @GetMapping("/client/sessions/{sessionId}/header")
    public ResponseEntity<ApiResponse<ClientChatRoomHeader>> getClientRoomHeader(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Client client
    ) {
        ClientChatRoomHeader data = chatService.getClientChatRoomHeader(sessionId, client.getId());
        return ResponseEntity.ok(ApiResponse.ok(data, "고객 채팅방 상단 헤더 조회 성공"));
    }


}
