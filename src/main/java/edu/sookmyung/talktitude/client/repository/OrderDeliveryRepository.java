package edu.sookmyung.talktitude.client.repository;

import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.client.model.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {
    Optional<OrderDelivery> findByOrder(Order order);
}
