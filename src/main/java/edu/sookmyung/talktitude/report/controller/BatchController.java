package edu.sookmyung.talktitude.report.controller;


import edu.sookmyung.talktitude.config.report.ReportSchedulerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class BatchController {
    private final ReportSchedulerConfig reportSchedulerConfig;

    // 리포트 생성 테스트용 - 즉시 실행
    @PostMapping("/run")
    public ResponseEntity<String> runReportBatch() {
        try {
            reportSchedulerConfig.runDailyReportGeneration();
            return ResponseEntity.ok("배치 실행 완료");
        } catch (Exception e) {
            log.error("배치 실행 실패", e);
            return ResponseEntity.status(500).body("배치 실행 실패: " + e.getMessage());
        }
    }
}