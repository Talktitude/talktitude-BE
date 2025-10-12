package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.common.service.S3Service;
import edu.sookmyung.talktitude.member.dto.PasswordUpdateRequest;
import edu.sookmyung.talktitude.member.dto.ProfileUpdateRequest;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import edu.sookmyung.talktitude.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final TokenService tokenService;

    // 프로필 수정
    @Transactional
    public void  updateProfile(Long memberId, ProfileUpdateRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (req.getCurrentPassword() == null || !passwordEncoder.matches(req.getCurrentPassword(), member.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_PASSWORD);
        }

        // 이름, 전화번호, 이메일 업데이트
        member.updateProfile(req.getName(), req.getPhone(), req.getEmail());

        // 프로필 이미지 업로드 (새 파일인 경우만)
        if (req.getProfileImage() != null && !req.getProfileImage().isEmpty()) {
            try {
                String imageUrl = s3Service.upload(req.getProfileImage());
                member.updateProfileImage(imageUrl);
            } catch (IOException e) {
                throw new BaseException(ErrorCode.S3_UPLOAD_FAILED);
            }
        }
    }

    // 비밀번호 수정
    @Transactional
    public void updatePassword(Long memberId, PasswordUpdateRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        // 현재 비밀번호 검증
        if (req.getCurrentPassword() == null || !passwordEncoder.matches(req.getCurrentPassword(), member.getPassword())) {
            throw new BaseException(ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호 확인 일치 검증
        if (req.getNewPassword() == null || req.getConfirmPassword() == null || !req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BaseException(ErrorCode.INVALID_PASSWORD_CONFIRM);
        }

        member.updatePassword(passwordEncoder.encode(req.getNewPassword()));
    }

    // 상담원 탈퇴
    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        // 이미 탈퇴한 회원 방지
        if (member.isDeleted()) {
            throw new BaseException(ErrorCode.ALREADY_DELETED_MEMBER);
        }

        // soft delete
        member.withdraw();

        // RefreshToken 삭제 (자동 로그아웃 효과)
        tokenService.deleteRefreshToken(member.getId(), member.getUserType());
    }
}
