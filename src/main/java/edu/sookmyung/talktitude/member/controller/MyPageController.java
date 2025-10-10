package edu.sookmyung.talktitude.member.controller;

import edu.sookmyung.talktitude.common.response.ApiResponse;
import edu.sookmyung.talktitude.member.dto.MemberUpdateRequest;
import edu.sookmyung.talktitude.member.dto.MyPageProfileDto;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members/me")
public class MyPageController {

    private final MyPageService myPageService;

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

    // 상담원 정보 수정
    @PatchMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Void>> updateMemberInfo(
            @AuthenticationPrincipal Member member,
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "email", required = false) String email,
            @RequestPart(value = "password", required = false) String password,
            @RequestPart(value = "passwordConfirm", required = false) String passwordConfirm,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        MemberUpdateRequest req = MemberUpdateRequest.builder()
                .name(name)
                .phone(phone)
                .email(email)
                .password(password)
                .passwordConfirm(passwordConfirm)
                .profileImage(profileImage)
                .build();

        myPageService.updateMemberInfo(member.getId(), req);
        return ResponseEntity.ok(ApiResponse.ok(null, "상담원 정보 수정 완료"));
    }
}
