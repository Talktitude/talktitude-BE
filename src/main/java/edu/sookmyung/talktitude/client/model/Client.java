package edu.sookmyung.talktitude.client.model;

import edu.sookmyung.talktitude.member.model.BaseUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
public class Client extends BaseUser {

    @Column(nullable = false, length = 100)
    private String address;
}
