package edu.sookmyung.talktitude.token.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(
        name = "refresh_token",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "user_type"})
        }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id",nullable=false)
    private Long userId;

    @Column(name="refresh_token",nullable = false)
    private String refreshToken;

    @Column(name="user_type",nullable=false)
    private String userType;

    public RefreshToken(Long userId, String refreshToken, String userType) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.userType = userType;
    }

    public RefreshToken update(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
        return this;
    }

}
