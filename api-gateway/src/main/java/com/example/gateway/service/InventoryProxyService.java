package com.example.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class InventoryProxyService {

    private static final Logger log = LoggerFactory.getLogger(InventoryProxyService.class);
    private final RestClient.Builder restClientBuilder;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public InventoryProxyService(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public ResponseEntity<String> getAllProducts() {
        log.info("Proxying get all products request to inventory service");

        RestClient restClient = restClientBuilder.baseUrl(inventoryServiceUrl).build();

        return restClient.get()
                .uri("/api/inventory/products")
                .retrieve()
                .toEntity(String.class);
    }
}
