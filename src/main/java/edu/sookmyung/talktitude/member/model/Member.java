package edu.sookmyung.talktitude.member.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Member extends BaseUser { //UserDetails를 상속받아 인증 객체로 사용

    @Column(nullable = false, length = 50)
    private String email;

    @Column(name = "is_filter", nullable = false)
    private boolean isFilter = true;

    public String getUserType() {
        return "Member";
    }

    @Override
    public void updateProfile(String name, String phone, String email) {
        super.updateProfile(name, phone, null);
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
    }
}





