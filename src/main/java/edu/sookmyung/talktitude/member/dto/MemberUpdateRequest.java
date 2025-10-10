package edu.sookmyung.talktitude.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberUpdateRequest {
    private String name;
    private String phone;
    private String email;
    private String password;
    private String passwordConfirm;
    private MultipartFile profileImage;
}
