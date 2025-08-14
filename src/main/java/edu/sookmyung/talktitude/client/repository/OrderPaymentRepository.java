package edu.sookmyung.talktitude.client.repository;

import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.client.model.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
   Optional<OrderPayment> findByOrder(Order order);

   @Query("SELECT op.paidAmount FROM OrderPayment op WHERE op.order.id = :orderId")
   Optional<Integer> findPaidAmountByOrderId(@Param("orderId") Long orderId);

}
