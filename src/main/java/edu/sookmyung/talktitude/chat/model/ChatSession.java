package edu.sookmyung.talktitude.chat.model;

import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.common.util.DateTimeUtils;
import edu.sookmyung.talktitude.member.model.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
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

    @ManyToOne
    @JoinColumn(name = "order_id") // nullable = true
    private Order order; // 선택한 주문 (없을 수도 있음)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = DateTimeUtils.nowKst(); // KST 고정
    }

    public void finish() {
        this.status = Status.FINISHED;
    }

}
