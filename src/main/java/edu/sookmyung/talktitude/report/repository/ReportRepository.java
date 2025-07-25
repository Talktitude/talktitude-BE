package edu.sookmyung.talktitude.report.repository;

import aj.org.objectweb.asm.commons.Remapper;
import edu.sookmyung.talktitude.report.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByCreatedAtBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1, Pageable pageable);



    @Query("SELECT r FROM Report r WHERE r.chatSession.client.loginId = :loginId")
    Page<Report> findByClientLoginId(@Param("loginId") String loginId,Pageable pageable);

    @Query("SELECT r FROM Report r WHERE UPPER(r.chatSession.client.name) LIKE UPPER(CONCAT('%', :clientName, '%')) AND DATE(r.createdAt) = :targetDate")
    Page<Report> findByClientNameLikeAndCreatedAt(String clientName, LocalDate targetDate, Pageable pageable);
}
