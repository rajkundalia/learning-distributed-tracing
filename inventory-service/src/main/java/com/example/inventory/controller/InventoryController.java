package com.example.inventory.controller;

import com.example.inventory.dto.ProductResponse;
import com.example.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }


    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        log.info("Received request to check product {} with quantity {}", productId, quantity);
        ProductResponse response = inventoryService.getProductById(productId, quantity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Received request to get all products");
        List<ProductResponse> products = inventoryService.getAllProducts();
        return ResponseEntity.ok(products);
    }
}
