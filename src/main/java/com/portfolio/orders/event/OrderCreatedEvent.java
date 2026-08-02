package com.portfolio.orders.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {

    private String orderId;
    private String customerName;
    private String item;
    private int quantity;
    private String createdAt;  // ISO-8601 timestamp
}
