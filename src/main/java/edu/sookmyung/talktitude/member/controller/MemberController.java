package edu.sookmyung.talktitude.member.controller;

import edu.sookmyung.talktitude.common.response.ApiResponse;
import edu.sookmyung.talktitude.member.dto.LoginRequest;
import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.dto.MemberDto;
import edu.sookmyung.talktitude.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody MemberDto dto) {
        memberService.register(dto);
        return ResponseEntity.ok(ApiResponse.ok(null, "회원가입이 완료되었습니다."));
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkDuplicate(@RequestParam String loginId) {
        boolean isDuplicate = memberService.isDuplicateLoginId(loginId);

        // 중복 시 서버 오류가 아닌 검증 메시지 띄움
        Map<String, Boolean> result = Map.of("isDuplicate", isDuplicate);
        String message = isDuplicate ? "이미 사용 중인 ID입니다." : "사용 가능한 ID입니다.";

        return ResponseEntity.ok(ApiResponse.ok(result, message));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        LoginResponse response = memberService.login(loginRequest.getLoginId(),loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }
}

