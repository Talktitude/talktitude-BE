package edu.sookmyung.talktitude.client.controller;

import edu.sookmyung.talktitude.client.dto.ClientDto;
import edu.sookmyung.talktitude.client.dto.ClientInfo;
import edu.sookmyung.talktitude.client.dto.OrderDetailInfo;
import edu.sookmyung.talktitude.client.dto.OrderInfo;
import edu.sookmyung.talktitude.client.service.ClientService;
import edu.sookmyung.talktitude.common.response.ApiResponse;
import edu.sookmyung.talktitude.member.dto.LoginRequest;
import edu.sookmyung.talktitude.member.dto.LoginResponse;
import edu.sookmyung.talktitude.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        LoginResponse response = clientService.login(loginRequest.getLoginId(),loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }

    /* 해당 채팅에 참가한 회원만 조회 가능하도록 구현 , */
    //오른쪽 정보 패널 -> 고객 정보 조회
    @GetMapping("/{sessionId}/client-info")
    public ResponseEntity<ApiResponse<ClientInfo>> getClientInfo(@AuthenticationPrincipal Member member, @PathVariable Long sessionId){

        ClientInfo clientInfo = clientService.getClientInfoById(sessionId, member);
        return ResponseEntity.ok(ApiResponse.ok(clientInfo));
    }

}