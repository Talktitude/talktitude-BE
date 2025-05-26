package edu.sookmyung.talktitude.client.model;


import jakarta.persistence.*;
import jakarta.persistence.criteria.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class OrderPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="order_id")
    private Order order;

    @Column(nullable=false)
    private int paidAmount;

    private String method;

    private int totalAmount;

    private int menuPrice;

    private int discountAmount;

    private int couponAmount;

    private int deliveryFee;
}
