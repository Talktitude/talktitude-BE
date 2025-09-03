package edu.sookmyung.talktitude.chat.recommend.facts;

import edu.sookmyung.talktitude.chat.model.ChatMessage;
import edu.sookmyung.talktitude.client.model.Order;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderFactsClient {

    public Map<String, Object> buildFacts(ChatMessage m, Order order) {
        Map<String, Object> facts = new HashMap<>();
        facts.put("sessionId", m.getChatSession().getId());
        facts.put("messageId", m.getId());
        if (order != null) {
            facts.put("orderId", order.getId());
            if (order.getRestaurant() != null) {
                facts.put("storeName", order.getRestaurant().getName());
            }
            // 필요 시 배달 ETA, 결제금액 등 추가
        }
        return facts;
    }
}