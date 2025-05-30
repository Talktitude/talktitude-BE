package edu.sookmyung.talktitude.chat.model;

import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.model.Order;
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
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

//    @OneToOne
//    @JoinColumn(name="order_id",nullable=false)
//    private Order order;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING) // ← enum 값을 문자열로 저장
    private Status status;

}
