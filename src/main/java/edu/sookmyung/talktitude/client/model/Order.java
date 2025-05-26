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
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int clientId;

    private int restaurantId;

    private LocalDateTime createdAt;

    private String orderNumber;

    @OneToOne(mappedBy="order", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    private OrderDelivery orderDelivery;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderMenu> orderMenus;

    @OneToOne(mappedBy="order", cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    private OrderPayment orderPayment;


}
