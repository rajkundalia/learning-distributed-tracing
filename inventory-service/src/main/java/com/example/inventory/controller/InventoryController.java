package com.example.inventory.controller;

import com.example.inventory.dto.ProductResponse;
import com.example.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Retrieves product details and validates stock availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found and stock available"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Requested quantity") @RequestParam Integer quantity) {

        log.info("Received request to check product {} with quantity {}", productId, quantity);
        ProductResponse response = inventoryService.getProductById(productId, quantity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Retrieves all products (with simulated delay for latency demonstration)")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Received request to get all products");
        List<ProductResponse> products = inventoryService.getAllProducts();
        return ResponseEntity.ok(products);
    }
}
