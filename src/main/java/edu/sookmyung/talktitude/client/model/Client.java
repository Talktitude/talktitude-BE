package edu.sookmyung.talktitude.client.model;

import edu.sookmyung.talktitude.member.model.BaseUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
public class Client extends BaseUser{

    @Column(nullable = false, length = 100)
    private String address;

    @OneToOne(mappedBy = "client")
    private Point point;

    @OneToMany(mappedBy = "client")
    private List<Coupon> coupons;


    public String getUserType() {
        return "Client";
    }
}
