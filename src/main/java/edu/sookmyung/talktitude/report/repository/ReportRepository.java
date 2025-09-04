package edu.sookmyung.talktitude.report.repository;

import edu.sookmyung.talktitude.report.model.Report;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByCreatedAtBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);

    @Query("SELECT r FROM Report r WHERE r.chatSession.client.loginId = :loginId")
    List<Report> findByClientLoginId(@Param("loginId") String loginId);

    @Query("SELECT r FROM Report r WHERE LOWER(r.chatSession.client.loginId) LIKE LOWER(CONCAT('%', :keyword, '%')) AND DATE(r.createdAt) = :targetDate")
    List<Report> findByClientLoginIdOrNameLikeAndCreatedAt(
            @Param("keyword") String keyword,
            @Param("targetDate") LocalDate targetDate
    );
}
