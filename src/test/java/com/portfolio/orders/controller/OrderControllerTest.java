package com.portfolio.orders.controller;

import com.portfolio.orders.model.Order;
import com.portfolio.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(orderService);
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        Order expectedOrder = Order.builder()
                .id("order-123")
                .customerName("John Doe")
                .item("Widget")
                .quantity(5)
                .status("CREATED")
                .createdAt(Instant.now())
                .build();

        when(orderService.createOrder("John Doe", "Widget", 5))
                .thenReturn(expectedOrder);

        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerName("John Doe");
        request.setItem("Widget");
        request.setQuantity(5);

        // Act
        ResponseEntity<Order> response = orderController.createOrder(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getCustomerName());
        assertEquals("Widget", response.getBody().getItem());
        assertEquals(5, response.getBody().getQuantity());
    }

    @Test
    void testCreateOrder_BlankCustomerName() {
        // Arrange
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerName("");
        request.setItem("Widget");
        request.setQuantity(5);

        // Act
        ResponseEntity<Order> response = orderController.createOrder(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateOrder_InvalidQuantity() {
        // Arrange
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerName("John");
        request.setItem("Widget");
        request.setQuantity(0);

        // Act
        ResponseEntity<Order> response = orderController.createOrder(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetOrderById_Found() {
        // Arrange
        Order order = Order.builder()
                .id("order-456")
                .customerName("Jane")
                .item("Gadget")
                .quantity(3)
                .status("CREATED")
                .createdAt(Instant.now())
                .build();

        when(orderService.getOrderById("order-456")).thenReturn(Optional.of(order));

        // Act
        ResponseEntity<Order> response = orderController.getOrderById("order-456");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getCustomerName());
    }

    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderService.getOrderById("non-existent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Order> response = orderController.getOrderById("non-existent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
