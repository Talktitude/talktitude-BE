package edu.sookmyung.talktitude.client.controller;

import edu.sookmyung.talktitude.client.dto.ClientDto;
import edu.sookmyung.talktitude.client.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody ClientDto dto) {
        clientService.register(dto);
        return ResponseEntity.ok("고객 회원가입 완료");
    }
}