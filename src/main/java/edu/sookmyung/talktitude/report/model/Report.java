package edu.sookmyung.talktitude.report.model;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="session_id", nullable=false)
    private ChatSession chatSession;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private Category category; //카테고리 처리

    @Column(name="summary_text",nullable=false)
    private String summaryText;

    @Column(name="created_at",nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();
}