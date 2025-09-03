package edu.sookmyung.talktitude.chat.repository;

import edu.sookmyung.talktitude.chat.model.Recommend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {
    List<Recommend> findByMessage_IdOrderByPriorityAsc(Long messageId);
}
