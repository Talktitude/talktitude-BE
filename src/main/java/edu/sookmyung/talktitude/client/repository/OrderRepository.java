package edu.sookmyung.talktitude.client.repository;

import edu.sookmyung.talktitude.client.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientLoginId(String clientLoginId);
    Optional<Order> findByOrderNumber(String orderNumber);
}
