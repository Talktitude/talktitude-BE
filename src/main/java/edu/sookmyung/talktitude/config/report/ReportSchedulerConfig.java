package edu.sookmyung.talktitude.config.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReportSchedulerConfig {

    private final JobLauncher jobLauncher;
    private final Job dailyReportGenerationJob;

    // 매일 자정 실행
    @Scheduled(cron = "0 0 12 * * *")
    public void runDailyReportGeneration()  {
        try {
            String targetDate = LocalDate.now().minusDays(1).toString(); //전날 종료된 상담 세션을 대상으로 진행
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", targetDate)
                    .addLong("timestamp", System.currentTimeMillis()) // 같은 Job 파라미터로 두 번 실행하는 것을 방지하기 위해 timestamp 패턴
                    .toJobParameters();

            log.info("일일 리포트 생성 배치 시작 - 날짜: {}",targetDate);
            JobExecution jobExecution = jobLauncher.run(dailyReportGenerationJob, jobParameters);
            log.info("배치 실행 완료 - 상태: {}", jobExecution.getStatus());

        }catch(Exception e) {
            log.error("일일 리포트 생성 배치 실행 실패",e);
        }
    }
}
