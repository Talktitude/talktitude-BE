package edu.sookmyung.talktitude.client.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    private String imageUrl;

    @Column(nullable=false)
    private String phone;

    @Column(nullable=false)
    private String address;

<<<<<<< HEAD
    @OneToMany(mappedBy="restaurant",cascade = CascadeType.ALL) //자식의 필드명
    private List<RestaurantMenu> menus;
=======
>>>>>>> 45d06875476d035b24e3f470ae72ded280edf710
}
