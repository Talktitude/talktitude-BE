package edu.sookmyung.talktitude.chat.dto.recommend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LlmSuggestItem {
    private String text;
    private List<String> policy_ids;
    private String risk; // "low"|"medium"|"high"
}
