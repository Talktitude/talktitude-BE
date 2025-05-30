package edu.sookmyung.talktitude.client.service;

import edu.sookmyung.talktitude.client.dto.ClientDto;
import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(ClientDto dto) {
        // 중복 로그인 ID 검사
        if (clientRepository.findByLoginId(dto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID입니다.");
        }

        // Client 엔티티 생성
        Client client = Client.builder()
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .isDeleted(false)
                .build();

        clientRepository.save(client);
    }
}
