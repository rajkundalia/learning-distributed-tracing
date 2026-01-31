package com.example.order.dto;

import java.util.List;

public class CreateBulkOrderRequest {
    private List<CreateOrderRequest> orders;

    public CreateBulkOrderRequest() {
    }

    public CreateBulkOrderRequest(List<CreateOrderRequest> orders) {
        this.orders = orders;
    }

    public List<CreateOrderRequest> getOrders() {
        return orders;
    }

    public void setOrders(List<CreateOrderRequest> orders) {
        this.orders = orders;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<CreateOrderRequest> orders;

        public Builder orders(List<CreateOrderRequest> orders) {
            this.orders = orders;
            return this;
        }

        public CreateBulkOrderRequest build() {
            return new CreateBulkOrderRequest(orders);
        }
    }
}
