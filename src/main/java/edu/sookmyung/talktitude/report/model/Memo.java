package edu.sookmyung.talktitude.report.model;

import edu.sookmyung.talktitude.chat.model.ChatSession;
import edu.sookmyung.talktitude.member.model.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private Member member;

    @ManyToOne
    @JoinColumn(name="session_id", nullable=false)
    private ChatSession chatSession;

    @Column(name="memo_text",nullable=false)
    private String memoText;

    @Column(name="is_deleted",nullable=false)
    private boolean isDeleted=false;

    private LocalDateTime createdAt = LocalDateTime.now();

    //memo update 기능을 위해 명시.
    private LocalDateTime updatedAt;
}
