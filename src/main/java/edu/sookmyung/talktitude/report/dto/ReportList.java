package edu.sookmyung.talktitude.report.dto;

import edu.sookmyung.talktitude.report.model.Category;
import edu.sookmyung.talktitude.report.model.Report;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportList {
    private Long id;
    private String clientName;
    private String phone;
    private Category category; //ENUM 대체
    private LocalDate createdAt;

    public static ReportList convertToDto(Report report) {
        return new ReportList(
                report.getId(),                                    // id
                report.getChatSession().getClient().getName(),           // clientName
                report.getChatSession().getClient().getPhone(),          // phoneNumber
                report.getCategory(),                              // category
                report.getCreatedAt().toLocalDate()
        );
    }

}
