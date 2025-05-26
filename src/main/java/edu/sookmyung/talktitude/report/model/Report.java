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
    private String category;

    @Column(nullable=false)
    private String summaryText;

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
