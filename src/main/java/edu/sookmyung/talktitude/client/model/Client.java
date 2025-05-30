package edu.sookmyung.talktitude.client.model;

import edu.sookmyung.talktitude.member.model.BaseUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Client extends BaseUser {

    @Column(nullable = false, length = 100)
    private String address;
}
