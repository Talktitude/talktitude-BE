package edu.sookmyung.talktitude.memo.dto;

import edu.sookmyung.talktitude.report.model.Memo;
import edu.sookmyung.talktitude.report.model.MemoPhase;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemoRequest {

    private String memoText;
}
