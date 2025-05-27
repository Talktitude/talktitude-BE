package edu.sookmyung.talktitude.client.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
<<<<<<< HEAD
=======
@Table(name = "orders") // "order"는 예약어 충돌 방지
>>>>>>> 45d06875476d035b24e3f470ae72ded280edf710
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "order_number", nullable = false, unique = true, length = 10)
    private String orderNumber;

    @OneToOne(mappedBy="order", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    private OrderDelivery orderDelivery;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderMenu> orderMenus;

    @OneToOne(mappedBy="order", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    private OrderPayment orderPayment;


}
