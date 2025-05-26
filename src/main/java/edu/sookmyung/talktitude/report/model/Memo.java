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
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name="session_id")
    private ChatSession chatSession;

    @Column(nullable=false)
    private String memoText;

    //디폴트 값 넣기
    @Column(nullable=false)
    private boolean isDeleted;

    private LocalDateTime createdAt;

    //memo update 기능을 위해 명시.
    private LocalDateTime updatedAt;
}
