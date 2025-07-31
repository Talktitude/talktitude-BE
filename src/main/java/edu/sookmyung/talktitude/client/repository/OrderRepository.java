package edu.sookmyung.talktitude.client.repository;

import edu.sookmyung.talktitude.client.model.Client;
import edu.sookmyung.talktitude.client.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientLoginId(String clientLoginId);
    Optional<Order> findByOrderNumber(String orderNumber);
}
