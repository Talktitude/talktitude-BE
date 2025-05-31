package edu.sookmyung.talktitude.token.repository;

import edu.sookmyung.talktitude.token.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserIdAndUserType(Long id, String userType);
}
