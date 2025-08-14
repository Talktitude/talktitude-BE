package edu.sookmyung.talktitude.client.model;

import edu.sookmyung.talktitude.chat.dto.OrderHistory;
import edu.sookmyung.talktitude.chat.model.ChatSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "orders") // "order"는 예약어 충돌 방지
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "order_number", nullable = false, unique = true, length = 10)
    private String orderNumber;

    @OneToMany(mappedBy = "order")
    private List<ChatSession> chatSessions;

    @OneToMany(mappedBy="order",fetch = FetchType.EAGER)
    private List<OrderMenu> orderMenus;

    @OneToOne(fetch = FetchType.EAGER)
    private OrderPayment orderPayment;

    @OneToOne(fetch = FetchType.EAGER)
    private OrderDelivery orderDelivery;
}