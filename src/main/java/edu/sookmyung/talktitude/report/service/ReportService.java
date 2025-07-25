package edu.sookmyung.talktitude.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sookmyung.talktitude.chat.dto.MessageInfo;
import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.model.Status;
import edu.sookmyung.talktitude.chat.repository.ChatSessionRepository;
import edu.sookmyung.talktitude.common.exception.BaseException;
import edu.sookmyung.talktitude.common.exception.ErrorCode;
import edu.sookmyung.talktitude.config.ai.GPTProperties;
import edu.sookmyung.talktitude.report.dto.*;
import edu.sookmyung.talktitude.report.model.Category;
import edu.sookmyung.talktitude.report.model.Report;
import edu.sookmyung.talktitude.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import edu.sookmyung.talktitude.chat.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {


    private final ChatClient chatClient;
    private final ChatService chatService;
    private final ReportRepository reportRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final GPTProperties gptProperties;
    private final ObjectMapper objectMapper;
    private final MemoService memoService;


    @Transactional
    public void generateReportsForDate(LocalDate targetDate) {
        //타겟 날짜에 해당하는 세션을 모두 들고와서 리포트 생성
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.plusDays(1).atStartOfDay();

        List<ChatSession> sessionsToProcess = chatSessionRepository
                .findFinishedSessionsWithoutReports(startOfDay, endOfDay, Status.FINISHED);

        for (ChatSession session : sessionsToProcess) {
                generateAndSaveReport(session.getId());
        }

    }

    //GPT API 응답을 처리해서 저장하는 메서드
    @Transactional
    public ReportDetail generateAndSaveReport(Long sessionId) {
        String data = generateReports(sessionId);

        ChatSession chatSession = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));

        ReportResponse reportResponse = parseResponse(data);
        Report report = new Report(
                null,
                chatSession,
                reportResponse.getCategory(),
                reportResponse.getSummary(),
                chatSession.getCreatedAt()
        );

        Report savedReport = reportRepository.save(report);

        return ReportDetail.convertToReportDetail(savedReport, Collections.emptyList());
    }

    // GPT API 응답 처리
    private ReportResponse parseResponse(String response){
        try{
            JsonNode node = objectMapper.readTree(response);

            String categoryStr = node.get("category").asText("기타");

            Category category;
            try{
                category = Category.valueOf(categoryStr.toUpperCase());
            }catch(IllegalArgumentException e){
                // 분류할 수 없는 카테고리가 응답으로 오면 기타로 분류
                category = Category.기타;
            }

            return new ReportResponse(
                    node.get("summary").asText("요약 정보 없음"),
                   category

            );
        }catch (Exception e){
            return new ReportResponse("요약 정보 없음",Category.기타);
        }
    }

    //실제 GPT API에게 요청을 보내는 메서드
    public String generateReports(Long sessionId){
        try {
            List<ChatMessage> messages = chatService.findChatMessage(sessionId);

            //채팅 내역을 문자열로 변환
            String messageData = formatMessageData(messages);

            GPTProperties.SummaryConfig config = gptProperties.getSummary();
            return chatClient.prompt()
                    .user(u -> u
                            .text(config.getSummaryPrompt() + messageData))
                    .options(OpenAiChatOptions.builder()
                            .model(config.getModel())
                            .temperature(config.getTemperature())
                            .maxTokens(config.getMaxTokens())
                            .build())
                    .call()
                    .content();
        }catch (Exception e){
            log.error("GPT API 호출 실패 - sessionId:{}, error:{}", sessionId, e.getMessage());
            throw new BaseException(ErrorCode.GPT_API_FAILED);
        }

    }

    // 채팅 내역을 GPT API에게 보낼 수 있는 형태로 변환
    private String formatMessageData(List<ChatMessage> messages) {
      try{
          List<MessageInfo> messageDtos = messages.stream()
                  .map(MessageInfo::convertToMessageInfo)
                  .collect(Collectors.toList());
          return objectMapper.writeValueAsString(messageDtos);
      }catch(JsonProcessingException e){
          throw new BaseException(ErrorCode.REPORT_JSON_PROCESSING_ERROR);
      }
    }


    // 날짜별 상담 목록 조회
    public Page<ReportList> getReportListsByDate(LocalDate date, Pageable pageable) {

        //하루의 시작 시간과 끝 시간
        LocalDateTime startOfDay = date.atStartOfDay(); //해당 날짜의 00:00:00 시간 변환
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return reportRepository.findByCreatedAtBetween(startOfDay,endOfDay,pageable)
                .map(ReportList::convertToDto);

    }

    // 상담 상세 내용 조회
    public ReportDetail getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new BaseException(ErrorCode.REPORT_NOT_FOUND));

        List<ReportMemo> reportMemos = memoService.getMemos(report.getId());

        return ReportDetail.convertToReportDetail(report,reportMemos);
    }

    //사용자 이름 검색을 통한 상담 목록 조회
    public Page<ReportList> searchReportLists(String clientName, LocalDate targetDate,Pageable pageable) {
        return reportRepository.findByClientNameLikeAndCreatedAt(clientName,targetDate,pageable)
                .map(ReportList::convertToDto);
    }

    // 고객별 상담 목록 조회 - 우측 패널용
    public Page<ReportListByClient> getReportsByClient(Long sessionId,Pageable pageable) {
        ChatSession chatSession = chatSessionRepository.findById(sessionId).orElseThrow(() -> new BaseException(ErrorCode.CHATSESSION_NOT_FOUND));
        return reportRepository.findByClientLoginId(chatSession.getClient().getLoginId(),pageable)
                .map(ReportListByClient::convertToReportListByClient);

    }

    // 고객별 상담 상세 조회 - 우측 패널용
    public ReportDetailByClient getReportDetailByClient(Long reportId) {

        return reportRepository.findById(reportId)
                .map(ReportDetailByClient::convertToReportDetailByClient)
                .orElseThrow(()-> new BaseException(ErrorCode.REPORT_NOT_FOUND));

    }
}
