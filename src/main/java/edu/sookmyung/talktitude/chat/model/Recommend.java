package edu.sookmyung.talktitude.chat.model;

import jakarta.persistence.*;
import jakarta.websocket.Decoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Recommend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @Column(columnDefinition = "TEXT")
    private String responseText;

    private int priority;

}
