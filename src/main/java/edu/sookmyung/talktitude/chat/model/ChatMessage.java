package edu.sookmyung.talktitude.chat.model;

import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.member.model.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    private Client client;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING) // ← enum 값을 문자열로 저장
    private Status status;

    private String email;

    private boolean isDeleted;

    private boolean isFilter;
}
