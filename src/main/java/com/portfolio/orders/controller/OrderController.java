package com.portfolio.orders.controller;

import com.portfolio.orders.model.Order;
import com.portfolio.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        // Validation
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getItem() == null || request.getItem().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getQuantity() < 1) {
            return ResponseEntity.badRequest().build();
        }

        Order order = orderService.createOrder(
                request.getCustomerName(),
                request.getItem(),
                request.getQuantity()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static class CreateOrderRequest {
        private String customerName;
        private String item;
        private int quantity;

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
