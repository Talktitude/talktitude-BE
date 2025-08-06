package edu.sookmyung.talktitude.report.controller;

import edu.sookmyung.talktitude.common.response.ApiResponse;
import edu.sookmyung.talktitude.common.response.PageResponse;
import edu.sookmyung.talktitude.member.model.Member;
import edu.sookmyung.talktitude.report.dto.ReportDetail;
import edu.sookmyung.talktitude.report.dto.ReportDetailByClient;
import edu.sookmyung.talktitude.report.dto.ReportList;
import edu.sookmyung.talktitude.report.dto.ReportListByClient;
import edu.sookmyung.talktitude.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    //날짜별 상담 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReportList>>> getReportListsByDate(@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date,@PageableDefault(size=10, sort="createdAt") Pageable pageable,@AuthenticationPrincipal Member member) {
        Page<ReportList> reportLists = reportService.getReportListsByDate(date,pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(reportLists)));
    }

    //특정 리포트 상세 내용 조회
    @GetMapping("/detail/{reportId}")
    public ResponseEntity<ApiResponse<ReportDetail>> getReportDetail(@PathVariable Long reportId,@AuthenticationPrincipal Member member) {
        ReportDetail reportDetail = reportService.getReportDetail(reportId);
        return ResponseEntity.ok(ApiResponse.ok(reportDetail));
    }

    //고객 이름으로 리포트 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ReportList>>> searchReportLists(@RequestParam String clientName, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, @PageableDefault(size=10, sort="createdAt") Pageable pageable,@AuthenticationPrincipal Member member) {
        Page<ReportList> reportLists = reportService.searchReportLists(clientName,date, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(reportLists)));
    }

    // 서비스 메서드 직접 호출 (테스트용)
    @PostMapping("/generate-reports-direct")
    public ResponseEntity<Map<String,Object>> generateReportsDirectly(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetDate) {
            long startTime = System.currentTimeMillis();
            reportService.generateReportsForDate(targetDate);
            long executionDuration = System.currentTimeMillis() - startTime;
            return ResponseEntity.ok(Map.of("success", true, "message","일일 리포트 생성이 완료되었습니다.",
                    "targetDate",targetDate.toString(),
                    "executionTime", executionDuration + "ms"));
    }

}
