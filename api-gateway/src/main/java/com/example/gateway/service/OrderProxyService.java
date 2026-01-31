package com.example.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OrderProxyService {

    private static final Logger log = LoggerFactory.getLogger(OrderProxyService.class);
    private final RestClient.Builder restClientBuilder;

    @Value("${order.service.url}")
    private String orderServiceUrl;

    public OrderProxyService(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public ResponseEntity<String> createOrder(String requestBody) {
        log.info("Proxying create order request to order service");

        RestClient restClient = restClientBuilder.baseUrl(orderServiceUrl).build();

        return restClient.post()
                .uri("/api/orders")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> createBulkOrders(String requestBody) {
        log.info("Proxying bulk order request to order service");

        RestClient restClient = restClientBuilder.baseUrl(orderServiceUrl).build();

        return restClient.post()
                .uri("/api/orders/bulk")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> getOrder(Long orderId) {
        log.info("Proxying get order request for order id: {}", orderId);

        RestClient restClient = restClientBuilder.baseUrl(orderServiceUrl).build();

        return restClient.get()
                .uri("/api/orders/{orderId}", orderId)
                .retrieve()
                .toEntity(String.class);
    }
}
