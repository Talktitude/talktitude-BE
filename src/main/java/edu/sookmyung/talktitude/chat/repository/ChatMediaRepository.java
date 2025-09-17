package edu.sookmyung.talktitude.chat.repository;

import edu.sookmyung.talktitude.chat.model.ChatMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMediaRepository extends JpaRepository<ChatMedia, Long> {
    List<ChatMedia> findByMessage_Id(Long messageId);
}
