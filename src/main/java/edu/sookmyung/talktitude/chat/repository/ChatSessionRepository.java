package edu.sookmyung.talktitude.chat.repository;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.chat.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
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

    // 상담 검색(상태 무관)
    List<ChatSession> findByMember_IdAndClient_LoginIdContainingIgnoreCase(
            Long memberId, String keyword);

    // 상담 검색(상태 필터)
    List<ChatSession> findByMember_IdAndStatusAndClient_LoginIdContainingIgnoreCase(
            Long memberId, Status status, String keyword);
           
  
    @Query("""
        SELECT cs FROM ChatSession cs 
        WHERE cs.createdAt >= :startDate 
        AND cs.createdAt < :endDate 
        AND cs.status = :status
        AND NOT EXISTS (
            SELECT 1 FROM Report r WHERE r.chatSession.id = cs.id
        )
        ORDER BY cs.createdAt ASC
        """)
    List<ChatSession> findFinishedSessionsWithoutReports(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") Status status  // ← 이렇게 매핑
    );



}
