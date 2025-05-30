package edu.sookmyung.talktitude.member.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Member extends BaseUser {

    @Column(nullable = false, length = 50)
    private String email;

    @Column(name = "is_filter", nullable = false)
    private boolean isFilter = true;
}





