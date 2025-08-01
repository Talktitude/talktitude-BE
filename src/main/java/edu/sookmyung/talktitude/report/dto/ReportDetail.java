package edu.sookmyung.talktitude.report.dto;

import edu.sookmyung.talktitude.memo.dto.MemoResponse;
import edu.sookmyung.talktitude.report.model.Category;
import edu.sookmyung.talktitude.report.model.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReportDetail {

    private Long id;
    private String clientName;
    private String memberName;
    private String createdAt;
    private Category category;
    private String phone;

    // 상담 내용 요약
    private String summaryText;

    // 상담 메모
    private List<MemoResponse> memos = new ArrayList<>();


    public static ReportDetail convertToReportDetail(Report report, List<MemoResponse> memos) {
        return new ReportDetail(
                report.getId(),
                report.getChatSession().getClient().getName(),
                report.getChatSession().getMember().getName(),
                report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm")),
                report.getCategory(),
                report.getChatSession().getClient().getPhone(),
                report.getSummaryText(),
                memos
        );
    }


}
