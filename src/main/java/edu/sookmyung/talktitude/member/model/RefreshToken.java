package edu.sookmyung.talktitude.member.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="member_id",nullable=false,unique=true)
    private Long memberId;

    @Column(name="refresh_token",nullable = false)
    private String refreshToken;

    @Column(name="user_type",nullable=false)
    private String userType;

    public RefreshToken(Long memberId, String refreshToken, String userType) {
        this.memberId = memberId;
        this.refreshToken = refreshToken;
        this.userType = userType;
    }

    public RefreshToken update(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
        return this;
    }

}
