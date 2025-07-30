package edu.sookmyung.talktitude.report.dto;

import edu.sookmyung.talktitude.report.model.Category;
import edu.sookmyung.talktitude.report.model.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailByClient {
    private Long id;
    private String time;
    private Category category;
    private String summaryText;

    public static ReportDetailByClient convertToReportDetailByClient(Report report) {
        return ReportDetailByClient.builder()
                .id(report.getId())
                .time(report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm")))
                .category(report.getCategory())
                .summaryText(report.getSummaryText())
                .build();
    }
}
