package edu.sookmyung.talktitude.client.repository;

import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.client.model.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
   Optional<OrderPayment> findByOrder(Order order);
}
