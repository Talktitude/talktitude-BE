package edu.sookmyung.talktitude.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyPageProfileDto {
    private String name;
    private String profileImageUrl;
    private String phoneNum;
    private String email;
}
