package com.portfolio.orders.service;

import com.portfolio.orders.event.OrderCreatedEvent;
import com.portfolio.orders.model.Order;
import com.portfolio.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, kafkaTemplate);
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        String customerName = "John Doe";
        String item = "Widget";
        int quantity = 5;

        Order savedOrder = Order.builder()
                .id("test-id-123")
                .customerName(customerName)
                .item(item)
                .quantity(quantity)
                .status("CREATED")
                .createdAt(Instant.now())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(customerName, item, quantity);

        // Assert
        assertNotNull(result);
        assertEquals(customerName, result.getCustomerName());
        assertEquals(item, result.getItem());
        assertEquals(quantity, result.getQuantity());
        assertEquals("CREATED", result.getStatus());

        // Verify Kafka message was sent
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(kafkaTemplate).send(eq("order-events"), eq("test-id-123"), eventCaptor.capture());

        OrderCreatedEvent sentEvent = eventCaptor.getValue();
        assertEquals("test-id-123", sentEvent.getOrderId());
        assertEquals(customerName, sentEvent.getCustomerName());
        assertEquals(item, sentEvent.getItem());
        assertEquals(quantity, sentEvent.getQuantity());
    }

    @Test
    void testGetAllOrders() {
        // Arrange
        Order order1 = Order.builder()
                .id("id-1")
                .customerName("Alice")
                .item("Item1")
                .quantity(2)
                .status("CREATED")
                .createdAt(Instant.now())
                .build();

        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(java.util.List.of(order1));

        // Act
        var orders = orderService.getAllOrders();

        // Assert
        assertEquals(1, orders.size());
        assertEquals("Alice", orders.get(0).getCustomerName());
        verify(orderRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void testGetOrderById_Found() {
        // Arrange
        Order order = Order.builder()
                .id("test-id")
                .customerName("Bob")
                .item("Item2")
                .quantity(1)
                .status("CREATED")
                .createdAt(Instant.now())
                .build();

        when(orderRepository.findById("test-id")).thenReturn(java.util.Optional.of(order));

        // Act
        var result = orderService.getOrderById("test-id");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Bob", result.get().getCustomerName());
    }

    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findById("non-existent")).thenReturn(java.util.Optional.empty());

        // Act
        var result = orderService.getOrderById("non-existent");

        // Assert
        assertFalse(result.isPresent());
    }
}
