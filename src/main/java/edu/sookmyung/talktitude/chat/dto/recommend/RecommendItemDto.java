package edu.sookmyung.talktitude.chat.dto.recommend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class RecommendItemDto {
    private Long id;
    private String text;
    private int priority;
    private List<String> policyIds;
}
