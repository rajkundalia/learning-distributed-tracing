package com.example.order.dto;

import com.example.order.entity.OrderStatus;
import java.time.LocalDateTime;

public class OrderResponse {
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public OrderResponse() {
    }

    public OrderResponse(Long orderId, Long productId, Integer quantity, OrderStatus status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Builder pattern implementation to support existing code
    public static class OrderResponseBuilder {
        private Long orderId;
        private Long productId;
        private Integer quantity;
        private OrderStatus status;
        private LocalDateTime createdAt;

        OrderResponseBuilder() {
        }

        public OrderResponseBuilder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderResponseBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public OrderResponseBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderResponseBuilder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public OrderResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OrderResponse build() {
            return new OrderResponse(orderId, productId, quantity, status, createdAt);
        }
    }

    public static OrderResponseBuilder builder() {
        return new OrderResponseBuilder();
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
