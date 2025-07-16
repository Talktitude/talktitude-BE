package edu.sookmyung.talktitude.chat.repository;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.sookmyung.talktitude.chat.model.Status;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // 상담원이 담당한 세션 중 상태별로 조회 (전체, 진행중, 종료)
    @Query("""
        SELECT cs FROM ChatSession cs
        WHERE cs.member.id = :memberId
        AND (:status IS NULL OR cs.status = :status)
        ORDER BY cs.createdAt DESC
    """)
    List<ChatSession> findByMemberAndStatus(
            @Param("memberId") Long memberId,
            @Param("status") Status status
    );
}
