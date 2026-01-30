package com.example.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryProxyService {

    private final RestClient.Builder restClientBuilder;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public ResponseEntity<String> getAllProducts() {
        log.info("Proxying get all products request to inventory service");

        RestClient restClient = restClientBuilder.baseUrl(inventoryServiceUrl).build();

        return restClient.get()
                .uri("/api/inventory/products")
                .retrieve()
                .toEntity(String.class);
    }
}
