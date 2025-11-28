package edu.sookmyung.talktitude.config.report;


import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.model.Status;
import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import edu.sookmyung.talktitude.report.dto.ReportDetail;
import edu.sookmyung.talktitude.report.repository.ReportRepository;
import edu.sookmyung.talktitude.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ReportBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ReportService reportService;
    private final ChatSessionRepository chatSessionRepository;
    private final ReportRepository reportRepository;

    //batch job 생성
    @Bean
    public Job dailyReportGenerationJob() {
        return new JobBuilder("dailyReportGenerationJob", jobRepository)
                .start(generateReportsStep())
                .build();
    }

    @Bean
    public Step generateReportsStep() {
        return new StepBuilder("generateReportsStep", jobRepository)
                .<ChatSession, ReportDetail>chunk(10,transactionManager)
                .reader(chatSessionReader(null))
                .processor(reportProcessor())
                .writer(reportWriter())
                .build();
    }

    @Bean
    @StepScope //step 실행 시점에 생성됨
    public ItemReader<ChatSession> chatSessionReader(@Value("#{jobParameters['targetDate']}") String targetDate) {
        return new ItemReader<ChatSession>() {
            private Iterator<ChatSession> sessionIterator;
            private boolean initialized = false;

            @Override
            public ChatSession read(){
                // db 조회는 처음 한번만 하기 위해서 initialized 변수 사용.
                if(!initialized){

                    LocalDate date = targetDate != null ? LocalDate.parse(targetDate) : LocalDate.now();

                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

                    //당일 종료된 상담 세션 가져오기
                    List<ChatSession> sessions =chatSessionRepository
                            .findFinishedSessionsWithoutReports(startOfDay, endOfDay, Status.FINISHED);

                    sessionIterator = sessions.iterator();
                    initialized = true;
                }
                return sessionIterator.hasNext() ? sessionIterator.next() : null;
            }
        };
    }

    //2. Processor
    @Bean
    public ItemProcessor<ChatSession, ReportDetail> reportProcessor() {
        return session -> {
            try {

                // ReportService의 generateAndSaveReport 메서드를 사용
                ReportDetail reportDetail = reportService.generateAndSaveReport(session.getId());

                log.info("세션 ID {} 리포트 생성 완료", session.getId());
                return reportDetail;

            } catch (Exception e) {
                log.error("세션 ID {} 리포트 생성 실패: {}", session.getId(), e.getMessage(), e);
                // 실패한 경우 null 반환하여 writer에서 처리하지 않음
                return null;
            }
        };
    }



    //3. Writer -> 로깅만 수행.
    @Bean
    public ItemWriter<ReportDetail> reportWriter() {
        return reportDetails -> {
            for (ReportDetail reportDetail : reportDetails) {
                if (reportDetail != null) {
                    log.info("리포트 생성 완료 - ID: {}, 고객명: {}, 카테고리: {}",
                            reportDetail.id(),
                            reportDetail.clientName(),
                            reportDetail.category());
                }
            }
            log.info("배치 처리 완료 - 총 {} 개 리포트 생성", reportDetails.size());
        };
    }
}