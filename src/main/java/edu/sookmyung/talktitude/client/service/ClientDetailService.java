package edu.sookmyung.talktitude.client.service;

import edu.sookmyung.talktitude.client.repository.ClientRepository;
import edu.sookmyung.talktitude.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ClientDetailService implements UserDetailsService {

    private final ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        return clientRepository.findByLoginId(loginId)
                .orElseThrow(()->new UsernameNotFoundException(loginId+"라는 User를 찾을 수 없습니다."));
    }
}
