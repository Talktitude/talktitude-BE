package edu.sookmyung.talktitude.member.controller;

import edu.sookmyung.talktitude.common.response.ApiResponse;
import edu.sookmyung.talktitude.member.dto.MyPageProfileDto;
import edu.sookmyung.talktitude.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/me")
public class MyPageController {

    // 상담원 정보 조회
    @GetMapping
    public ResponseEntity<ApiResponse<MyPageProfileDto>> getMyPageProfile(
            @AuthenticationPrincipal Member member
    ) {
        MyPageProfileDto dto = new MyPageProfileDto(
                member.getName(),
                member.getProfileImageUrl(),
                member.getPhone(),
                member.getEmail()
        );
        return ResponseEntity.ok(ApiResponse.ok(dto, "상담원 정보 조회 성공"));
    }
}
