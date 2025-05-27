package edu.sookmyung.talktitude.client.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", unique = true)
    private String loginId;

    @Column(nullable = false, length = 60)
    private String passWord;

    @Column(length = 10)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false, length = 100)
    private String address;
}
