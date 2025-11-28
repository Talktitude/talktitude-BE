package edu.sookmyung.talktitude.chat.recommend.facts;

import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.client.model.Order;

import edu.sookmyung.talktitude.client.model.OrderMenu;
import edu.sookmyung.talktitude.client.repository.OrderMenuRepository;
import edu.sookmyung.talktitude.client.repository.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
// 주문 사실값 생성
public class OrderFactsClient {

    private final OrderMenuRepository orderMenuRepository;
    private final OrderPaymentRepository orderPaymentRepository;

    public Map<String, Object> buildFacts(ChatMessage m, Order order) {
        Map<String, Object> facts = new HashMap<>();
        facts.put("sessionId", m.getChatSession().getId());
        facts.put("messageId", m.getId());

        if (order != null) {
            facts.put("orderId", order.getId());

            if (order.getRestaurant() != null) {
                facts.put("storeName", order.getRestaurant().getName());
            }

            // 주문 시간(보기 좋은 포맷)
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            if (order.getCreatedAt() != null) {
                facts.put("orderCreatedAt", order.getCreatedAt().format(fmt));
            }

            // 메뉴 목록/개수
            List<OrderMenu> menus = orderMenuRepository.findByOrderId(order.getId());
            facts.put("menuNames", menus.stream().map(OrderMenu::getMenu).toList());
            facts.put("menuCount", menus.size());
            if (!menus.isEmpty()) {
                String first = menus.get(0).getMenu();
                int others = Math.max(0, menus.size() - 1);
                facts.put("menuSummary", (others > 0) ? first + " 외 " + others + "개" : first);
            }

            // 결제 금액
            orderPaymentRepository.findPaidAmountByOrderId(order.getId())
                    .ifPresent(paid -> facts.put("paidAmountWon", String.format("%,d원", paid)));
        }
        return facts;
    }
}