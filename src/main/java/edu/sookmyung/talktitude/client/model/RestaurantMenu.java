package edu.sookmyung.talktitude.client.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class RestaurantMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="restaurant_id",nullable = false) // 자식 테이블의 FK 컬럼명.
    private Restaurant restaurant;

    @Column(nullable=false)
    private String menu;

    @Column(nullable=false)
    private int price;

}
