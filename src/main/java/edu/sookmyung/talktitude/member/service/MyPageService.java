package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.common.service.S3Service;
import edu.sookmyung.talktitude.member.dto.MemberUpdateRequest;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
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

    @Transactional
    public void  updateMemberInfo(Long memberId, MemberUpdateRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 변경
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            if (req.getPasswordConfirm() == null || !req.getPassword().equals(req.getPasswordConfirm())) {
                throw new BaseException(ErrorCode.INVALID_PASSWORD_CONFIRM);
            }
            member.updatePassword(passwordEncoder.encode(req.getPassword()));
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
}
