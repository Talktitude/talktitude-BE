package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.model.Member;

public interface MemberService {
    public LoginResponse login(String loginId, String password);
    public Member findMemberById(Long id);
}
