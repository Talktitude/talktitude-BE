package edu.sookmyung.talktitude.memo.dto;

import edu.sookmyung.talktitude.report.model.Memo;
import edu.sookmyung.talktitude.report.model.MemoPhase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record MemoResponse(
        Long id,
        String profileImageUrl,
        String createdAt,
        String memberLoginId,
        String memberName,
        String memoText,
        MemoPhase memoPhase
) {
    public static MemoResponse convertToMemoResponse(Memo memo) {
        String profileImageUrl = null;

        if (memo.getMemoPhase() == MemoPhase.AFTER_CHAT) {
            profileImageUrl = memo.getMember().getProfileImageUrl();
        }

        String createdAt = (memo.getMemoPhase() == MemoPhase.DURING_CHAT)
                ? memo.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : memo.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


        return new MemoResponse(
                memo.getId(),
                profileImageUrl,
                createdAt,
                memo.getMember().getLoginId(),
                memo.getMember().getName(),
                memo.getMemoText(),
                memo.getMemoPhase()
        );
    }
}