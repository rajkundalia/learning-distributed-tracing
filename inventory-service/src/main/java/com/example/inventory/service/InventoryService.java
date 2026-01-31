package com.example.inventory.service;

import com.example.inventory.dto.ProductResponse;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.repository.ProductRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final ProductRepository productRepository;
    private final Tracer tracer;

    public InventoryService(ProductRepository productRepository, Tracer tracer) {
        this.productRepository = productRepository;
        this.tracer = tracer;
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId, Integer requestedQuantity) {
        log.info("Fetching product with id: {}, requested quantity: {}", productId, requestedQuantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format("Product with id %d not found", productId)));

        // Add custom span attributes
        Span currentSpan = Span.current();
        currentSpan.setAttribute("product.id", productId);
        currentSpan.setAttribute("product.name", product.getName());
        currentSpan.setAttribute("stock.available", product.getStockQuantity());
        currentSpan.setAttribute("stock.requested", requestedQuantity);

        // Add span event
        currentSpan.addEvent("stock-checked");

        if (product.getStockQuantity() < requestedQuantity) {
            log.warn("Insufficient stock for product {}. Requested: {}, Available: {}",
                    productId, requestedQuantity, product.getStockQuantity());
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %d. Requested: %d, Available: %d",
                            productId, requestedQuantity, product.getStockQuantity()));
        }

        log.info("Product {} has sufficient stock", productId);
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products with simulated delay");

        // Simulate slow database query for Flow 4
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted during simulated delay", e);
        }

        List<Product> products = productRepository.findAll();
        log.info("Retrieved {} products", products.size());

        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .stockQuantity(product.getStockQuantity())
                .price(product.getPrice())
                .build();
    }
}
