package edu.sookmyung.talktitude.client.repository;

import edu.sookmyung.talktitude.client.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByLoginId(String loginId);
}

