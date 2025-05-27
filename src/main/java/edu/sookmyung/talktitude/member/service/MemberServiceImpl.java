package edu.sookmyung.talktitude.member.service;

import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(()-> new IllegalStateException("Member not found"));
    }
}
