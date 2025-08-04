package edu.sookmyung.talktitude.report.dto;

import edu.sookmyung.talktitude.memo.dto.MemoResponse;
import edu.sookmyung.talktitude.report.model.Category;
import edu.sookmyung.talktitude.report.model.Report;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public record ReportDetail (
     Long id,
     String clientName,
     String memberName,
     String createdAt,
     Category category,
     String phone,
    // 상담 내용 요약
     String summaryText,
    // 상담 메모
     List<MemoResponse> memos
){

    public static ReportDetail convertToReportDetail(Report report, List<MemoResponse> memos) {
        return new ReportDetail(
                report.getId(),
                report.getChatSession().getClient().getName(),
                report.getChatSession().getMember().getName(),
                report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm")),
                report.getCategory(),
                report.getChatSession().getClient().getPhone(),
                report.getSummaryText(),
                memos != null ? memos : new ArrayList<>()
        );
    }
}

