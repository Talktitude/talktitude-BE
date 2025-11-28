package edu.sookmyung.talktitude.report.dto;

import edu.sookmyung.talktitude.report.model.Category;
import edu.sookmyung.talktitude.report.model.Report;

import java.time.format.DateTimeFormatter;

public record ReportListByClient(
        Long id,
        String time,
        Category category,
        String summaryText
) {
    public static ReportListByClient convertToReportListByClient(Report report) {
        return new ReportListByClient(
                report.getId(),
                report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm")),
                report.getCategory(),
                report.getSummaryText()
        );
    }
}