package edu.sookmyung.talktitude.chat.repository;

import edu.sookmyung.talktitude.chat.model.ChatMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMediaRepository extends JpaRepository<ChatMedia, Long> {
    List<ChatMedia> findByMessage_Id(Long messageId);

    // 여러 메시지에 대한 미디어를 한 번에 조회(N+1 방지)
    List<ChatMedia> findByMessage_IdIn(List<Long> messageIds);
}
