package edu.sookmyung.talktitude.member.controller;

import edu.sookmyung.talktitude.member.dto.LoginRequest;
import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.dto.MemberDto;
import edu.sookmyung.talktitude.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody MemberDto dto) {
        memberService.register(dto);
        return ResponseEntity.ok("회원가입 완료");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        LoginResponse response = memberService.login(loginRequest.getLoginId(),loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }
}

