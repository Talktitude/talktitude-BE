package edu.sookmyung.talktitude.memo.controller;

import edu.sookmyung.talktitude.common.response.ApiResponse;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.memo.dto.MemoResponse;
import edu.sookmyung.talktitude.memo.service.MemoService;
import edu.sookmyung.talktitude.memo.dto.MemoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/memos")
public class MemoController {

    private final MemoService memoService;

    //메모 등록 기능
    @PostMapping("/register/{sessionId}")
    public ResponseEntity<ApiResponse<MemoResponse>> registerMemos(@PathVariable Long sessionId, @RequestBody MemoRequest memo, @AuthenticationPrincipal Member member) {
        MemoResponse memoResponse = memoService.registerMemos(sessionId,memo,member);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(memoResponse));
    }

    //메모 삭제 기능
    @DeleteMapping("delete/{memoId}")
    public ResponseEntity<ApiResponse<String>> deleteMemo(@PathVariable Long memoId, @AuthenticationPrincipal Member member) {
        memoService.deleteMemo(memoId,member);
        return ResponseEntity.ok(ApiResponse.ok("메모가 삭제되었습니다."));
    }
}
