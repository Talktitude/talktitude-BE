package edu.sookmyung.talktitude.chat.controller;

import edu.sookmyung.talktitude.chat.dto.ChatSessionDetailDto;
import edu.sookmyung.talktitude.chat.dto.ChatSessionDto;
import edu.sookmyung.talktitude.chat.dto.CreateSessionRequest;
import edu.sookmyung.talktitude.chat.service.ChatService;
import edu.sookmyung.talktitude.common.response.ApiResponse;
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

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 상담 세션 생성
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createSession(@RequestBody CreateSessionRequest request) {
        Long sessionId = chatService.createChatSession(request);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("sessionId", sessionId), "상담 세션이 생성되었습니다."));
    }

    // 상담원 상담 목록 조회
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatSessionDto>>> getSessions(
            @AuthenticationPrincipal Member member,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        List<ChatSessionDto> result = chatService.getChatSessionsForMember(member.getId(), status);
        return ResponseEntity.ok(ApiResponse.ok(result, "상담 목록 조회 성공"));
    }

    // 채팅 세션 정보 조회
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<ChatSessionDetailDto>> getChatSessionDetail(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Member member
    ) {
        ChatSessionDetailDto session = chatService.getChatSessionDetail(sessionId, member.getId());
        return ResponseEntity.ok(ApiResponse.ok(session, "상담 세션 상세 조회 성공"));
    }

    // 상담 종료
    @PatchMapping("/sessions/{sessionId}/finish")
    public ResponseEntity<ApiResponse<Void>> finishSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Member member
    ) {
        chatService.finishChatSession(sessionId, member.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "상담이 정상적으로 종료되었습니다."));
    }

}
