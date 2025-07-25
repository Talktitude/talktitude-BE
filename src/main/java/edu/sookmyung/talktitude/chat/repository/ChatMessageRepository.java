package edu.sookmyung.talktitude.chat.repository;

import edu.sookmyung.talktitude.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import java.util.Optional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatSessionId(Long sessionId);
    //특정 세션에서 가장 마지막에 작성된 메시지 1개를 시간 기준으로 조회
    Optional<ChatMessage> findTopByChatSessionOrderByCreatedAtDesc(ChatSession chatSession);

}
