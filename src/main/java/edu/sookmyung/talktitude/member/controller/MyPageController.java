package edu.sookmyung.talktitude.member.controller;

import edu.sookmyung.talktitude.common.response.ApiResponse;
import edu.sookmyung.talktitude.member.dto.PasswordUpdateRequest;
import edu.sookmyung.talktitude.member.dto.ProfileUpdateRequest;
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

    // 상담원 프로필 수정 (이름, 전화번호, 이메일, 사진)
    @PatchMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal Member member,
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "email", required = false) String email,
            @RequestPart(value = "currentPassword", required = false) String currentPassword,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        ProfileUpdateRequest req = ProfileUpdateRequest.builder()
                .name(name)
                .phone(phone)
                .email(email)
                .currentPassword(currentPassword)
                .profileImage(profileImage)
                .build();

        myPageService.updateProfile(member.getId(), req);
        return ResponseEntity.ok(ApiResponse.ok(null, "상담원 정보 수정 완료"));
    }

    // 상담원 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @RequestBody PasswordUpdateRequest req,
            @AuthenticationPrincipal Member member
    ) {
        myPageService.updatePassword(member.getId(), req);
        return ResponseEntity.ok(ApiResponse.ok(null, "비밀번호 변경 완료"));
    }
}
