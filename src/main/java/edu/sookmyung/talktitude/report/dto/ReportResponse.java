package edu.sookmyung.talktitude.report.dto;

import edu.sookmyung.talktitude.report.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private String summary;
    private Category category;
}
