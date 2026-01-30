package com.example.gateway.controller;

import com.example.gateway.service.InventoryProxyService;
import com.example.gateway.service.OrderProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Gateway", description = "Gateway APIs for routing requests to downstream services")
public class GatewayController {

    private final OrderProxyService orderProxyService;
    private final InventoryProxyService inventoryProxyService;

    @PostMapping("/orders")
    @Operation(summary = "Create order via gateway", description = "Routes order creation request to Order Service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<String> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Order creation request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"productId\": 1, \"quantity\": 2}"))) @RequestBody String requestBody) {

        log.info("Received order creation request at gateway");
        return orderProxyService.createOrder(requestBody);
    }

    @PostMapping("/orders/bulk")
    @Operation(summary = "Create bulk orders via gateway", description = "Routes bulk order creation request to Order Service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bulk orders created successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock for one or more products")
    })
    public ResponseEntity<String> createBulkOrders(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Bulk order creation request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"orders\": [{\"productId\": 1, \"quantity\": 2}, {\"productId\": 2, \"quantity\": 1}]}"))) @RequestBody String requestBody) {

        log.info("Received bulk order creation request at gateway");
        return orderProxyService.createBulkOrders(requestBody);
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get order by ID via gateway", description = "Routes get order request to Order Service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<String> getOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {

        log.info("Received get order request at gateway for order id: {}", orderId);
        return orderProxyService.getOrder(orderId);
    }

    @GetMapping("/inventory/products")
    @Operation(summary = "Get all products via gateway", description = "Routes get all products request to Inventory Service (with simulated delay)")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public ResponseEntity<String> getAllProducts() {
        log.info("Received get all products request at gateway");
        return inventoryProxyService.getAllProducts();
    }
}
