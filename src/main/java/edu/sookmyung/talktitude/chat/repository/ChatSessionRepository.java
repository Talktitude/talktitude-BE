package edu.sookmyung.talktitude.chat.repository;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.sookmyung.talktitude.chat.model.Status;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // 전체 조회
    List<ChatSession> findByMemberId(Long memberId);

    // 상태 필터 조회
    @Query("""
        SELECT cs FROM ChatSession cs
        WHERE cs.member.id = :memberId
        AND cs.status = :status
        ORDER BY cs.createdAt DESC
    """)
    List<ChatSession> findByMemberAndStatus(
            @Param("memberId") Long memberId,
            @Param("status") Status status
    );
}
