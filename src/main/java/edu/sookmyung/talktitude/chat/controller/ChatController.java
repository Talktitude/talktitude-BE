package edu.sookmyung.talktitude.chat.controller;

import edu.sookmyung.talktitude.chat.dto.ChatSessionDetailDto;
import edu.sookmyung.talktitude.chat.dto.ChatSessionDto;
import edu.sookmyung.talktitude.chat.dto.CreateSessionRequest;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.service.ChatService;
import edu.sookmyung.talktitude.member.model.Member;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 상담 세션 생성
    @PostMapping("/sessions")
    public ResponseEntity<String> createSession(@RequestBody CreateSessionRequest request) {
        Long sessionId = chatService.createChatSession(request);
        return ResponseEntity.ok("상담 세션이 생성되었습니다. ID: " + sessionId);
    }

    // 상담원 상담 목록 조회
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDto>> getSessions(
            @AuthenticationPrincipal Member member,
            @RequestParam(defaultValue = "ALL") String status
    ) {
        List<ChatSessionDto> result = chatService.getChatSessionsForMember(member.getId(), status);
        return ResponseEntity.ok(result);
    }

    // 채팅 세션 정보 조회
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionDetailDto> getChatSessionDetail(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Member member
    ) {
        ChatSessionDetailDto session = chatService.getChatSessionDetail(sessionId, member.getId());
        return ResponseEntity.ok(session);
    }

}
