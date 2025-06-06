package edu.sookmyung.talktitude.client.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class OrderDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="order_id", nullable=false)
    private Order order;

    @Column(nullable=false)
    private String phone;

    @Column(nullable=false)
    private String address;

    @Column(name="deliver_note",columnDefinition = "TEXT")
    private String deliveryNote;

    @Column(name="restaurant_note",columnDefinition = "TEXT")
    private String restaurantNote;
}
