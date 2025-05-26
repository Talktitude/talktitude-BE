package edu.sookmyung.talktitude.client.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="order_id",nullable=false)
    private Order order;

    @Column(nullable=false)
    private String menu;

    @Column(nullable=false)
    private int price;

    @Column(nullable=false)
    private int quantity;

}
