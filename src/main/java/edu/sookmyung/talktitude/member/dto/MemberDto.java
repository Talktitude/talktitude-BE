package edu.sookmyung.talktitude.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private String loginId;
    private String password;
    private String name;
    private String phone;
    private String email;
}

