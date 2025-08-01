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
public class MemoResponse {

    private Long id;
    private String profileImageUrl; //null값 허용
    private String createdAt;
    private String memberLoginId;     //메모 작성자 아이디
    private String memberName;
    private String memoText;
    private MemoPhase memoPhase;

    public static MemoResponse convertToMemoResponse(Memo memo){

        String profileImageUrl = null;

        if(memo.getMemoPhase()== MemoPhase.AFTER_CHAT){
            profileImageUrl = memo.getMember().getProfileImageUrl();
        }

        String createdAt = (memo.getCreatedAt() != null)  ? memo.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return MemoResponse.builder()
                .id(memo.getId())
                .memoText(memo.getMemoText())
                .memberLoginId(memo.getMember().getLoginId())
                .memberName(memo.getMember().getName())
                .createdAt(createdAt)
                .profileImageUrl(profileImageUrl)
                .memoPhase(memo.getMemoPhase())
                .build();
    }

}
