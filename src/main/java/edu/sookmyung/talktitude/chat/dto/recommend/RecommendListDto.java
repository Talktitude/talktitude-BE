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
public class RecommendListDto {
    private Long messageId;
    private List<RecommendItemDto> items;
}
