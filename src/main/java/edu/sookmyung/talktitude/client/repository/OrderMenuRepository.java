package edu.sookmyung.talktitude.client.repository;

import edu.sookmyung.talktitude.client.model.Order;
import edu.sookmyung.talktitude.client.model.OrderMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderMenuRepository extends JpaRepository<OrderMenu, Long> {

    List<OrderMenu> findByOrder(Order order);
}
