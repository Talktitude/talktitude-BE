package edu.sookmyung.talktitude.chat.dto.recommend;

import edu.sookmyung.talktitude.chat.model.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
// 테스트용
public class CreateMessageRequest {
    private SenderType senderType;   // CLIENT | USER
    private String originalText;     // 고객이 입력한 문장
}
