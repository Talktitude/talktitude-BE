package edu.sookmyung.talktitude.chat.repository;

import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    //특정 세션에서 가장 마지막에 작성된 메시지 1개를 시간 기준으로 조회
    Optional<ChatMessage> findTopByChatSessionOrderByCreatedAtDesc(ChatSession chatSession);
}
