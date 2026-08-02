package com.portfolio.orders.service;

import com.portfolio.orders.event.OrderCreatedEvent;
import com.portfolio.orders.model.Order;
import com.portfolio.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public Order createOrder(String customerName, String item, int quantity) {
        // Create and save the order
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .customerName(customerName)
                .item(item)
                .quantity(quantity)
                .status("CREATED")
                .createdAt(Instant.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Publish the event to Kafka
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .customerName(savedOrder.getCustomerName())
                .item(savedOrder.getItem())
                .quantity(savedOrder.getQuantity())
                .createdAt(savedOrder.getCreatedAt().toString())
                .build();

        kafkaTemplate.send("order-events", savedOrder.getId(), event);

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }
}
