package edu.sookmyung.talktitude.client.model;

import jakarta.persistence.*;
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

    @OneToOne
    @JoinColumn(name="order_id", nullable=false)
    private Order order;

    @Column(name="paid_amount",nullable=false)
    private int paidAmount =0;

    @Enumerated(EnumType.STRING)
    private Method method = Method.카드;

    @Column(name="total_amount",nullable=false)
    private int totalAmount=0;

    @Column(name="menu_price",nullable=false)
    private int menuPrice=0;

    @Column(name="discount_amount",nullable=false)
    private int discountAmount=0;

    @Column(name="coupon_amount",nullable=false)
    private int couponAmount=0;

    @Column(name="delivery_fee",nullable=false)
    private int deliveryFee=0;
}
