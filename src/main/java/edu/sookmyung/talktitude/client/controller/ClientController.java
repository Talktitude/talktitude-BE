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
import edu.sookmyung.talktitude.memo.dto.MemoResponse;
import edu.sookmyung.talktitude.report.dto.ReportListByClient;
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
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest){
        LoginResponse response = clientService.login(loginRequest.getLoginId(),loginRequest.getPassword());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    //오른쪽 정보 패널 -> 고객 정보 조회
    @GetMapping("/{sessionId}/client-info")
    public ResponseEntity<ApiResponse<ClientInfo>> getClientInfo(@AuthenticationPrincipal Member member, @PathVariable Long sessionId){
        ClientInfo clientInfo = clientService.getClientInfoById(sessionId, member);
        return ResponseEntity.ok(ApiResponse.ok(clientInfo));
    }

    //오른쪽 정보 패널 -> 주문 정보 조회
    @GetMapping("/{sessionId}/orders")
    public ResponseEntity<ApiResponse<List<OrderInfo>>> getOrder(@AuthenticationPrincipal Member member, @PathVariable Long sessionId){

        List<OrderInfo> orderInfoList = clientService.getOrderById(sessionId, member); //chatsession -> client -> order를 찾는 방식
        return ResponseEntity.ok(ApiResponse.ok(orderInfoList));
    }

    //오른쪽 정보 패널 -> 상세 주문 정보 조회
    @GetMapping("/{sessionId}/orders/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderDetailInfo>> getOrderDetail(@AuthenticationPrincipal Member member,  @PathVariable Long sessionId,
                                                                       @PathVariable String orderNumber) {
        OrderDetailInfo orderDetailInfo = clientService.getOrderDetailById(orderNumber,member,sessionId);
        return ResponseEntity.ok(ApiResponse.ok(orderDetailInfo));
    }

    //오른쪽 정보 패널 -> 고객별 상담 목록 조회
    @GetMapping("/{sessionId}/reports")
    public ResponseEntity<ApiResponse<List<ReportListByClient>>> getReportListByClient(@PathVariable Long sessionId, @AuthenticationPrincipal Member member) {
        List<ReportListByClient> reportListByClients = clientService.getReportsByClient(sessionId,member);
        return ResponseEntity.ok(ApiResponse.ok(reportListByClients));
    }

    //오른쪽 정보 패널 -> 상담 중에 작성된 메모만 조회
    @GetMapping("/{sessionId}/during-session")
    public ResponseEntity<ApiResponse<List<MemoResponse>>> getDuringChatUserMemos(@PathVariable Long sessionId, @AuthenticationPrincipal Member member) {
        List<MemoResponse> reportMemos = clientService.getDuringChatUserMemos(sessionId,member);
        return ResponseEntity.ok().body(ApiResponse.ok(reportMemos));
    }

}