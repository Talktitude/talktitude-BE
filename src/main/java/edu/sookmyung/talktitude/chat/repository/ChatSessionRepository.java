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
public interface ChatSessionRepository extends JpaRepository<ChatSession,Long>{

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
