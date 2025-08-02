package edu.sookmyung.talktitude.memo.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemoRequest {

    private String memoText;
}
