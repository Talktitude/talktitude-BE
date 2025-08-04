package edu.sookmyung.talktitude.report.dto;

import edu.sookmyung.talktitude.report.model.Category;
import edu.sookmyung.talktitude.report.model.Report;
import java.time.LocalDate;


public record ReportList (
     Long id,
     String clientName,
     String phone,
     Category category,
     LocalDate createdAt
){

    public static ReportList convertToDto(Report report) {
        return new ReportList(
                report.getId(),
                report.getChatSession().getClient().getName(),
                report.getChatSession().getClient().getPhone(),
                report.getCategory(),
                report.getCreatedAt().toLocalDate()
        );
    }

}
