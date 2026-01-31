package com.example.order.client;

import com.example.order.dto.ProductResponse;
import com.example.order.exception.InsufficientStockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);
    private final RestClient.Builder restClientBuilder;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public InventoryClient(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public ProductResponse checkStock(Long productId, Integer quantity) {
        log.info("Calling inventory service to check stock for product {} with quantity {}", productId, quantity);

        try {
            RestClient restClient = restClientBuilder.baseUrl(inventoryServiceUrl).build();

            ProductResponse response = restClient.get()
                    .uri("/api/inventory/{productId}?quantity={quantity}", productId, quantity)
                    .retrieve()
                    .body(ProductResponse.class);

            log.info("Stock check successful for product {}", productId);
            return response;

        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                log.error("Insufficient stock for product {}", productId);
                throw new InsufficientStockException(ex.getMessage());
            }
            log.error("Error checking stock for product {}: {}", productId, ex.getMessage());
            throw ex;
        }
    }
}
