package edu.sookmyung.talktitude.chat.repository;

import edu.sookmyung.talktitude.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import java.util.Optional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 세션의 최신 메시지 1건
    Optional<ChatMessage> findTopByChatSessionOrderByCreatedAtDesc(ChatSession session);

    // 세션의 모든 메시지 (시간순 정렬 권장)
    List<ChatMessage> findByChatSessionIdOrderByCreatedAtAsc(Long sessionId);

    // 사용 중이면 그대로
    default List<ChatMessage> findByChatSessionId(Long sessionId) {
        return findByChatSessionIdOrderByCreatedAtAsc(sessionId);
    }

    // 세션의 최신 메시지 n건
    @Query("SELECT msg FROM ChatMessage msg WHERE msg.chatSession.id = :sessionId ORDER BY msg.createdAt DESC LIMIT :count")
    List<ChatMessage> findRecentByChatSessionId(@Param("sessionId") Long sessionId, @Param("count") int count);

}
