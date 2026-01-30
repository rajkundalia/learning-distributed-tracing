package com.example.order.controller;

import com.example.order.dto.CreateBulkOrderRequest;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order after validating stock availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<OrderResponse> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Order creation request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"productId\": 1, \"quantity\": 2}"))) @RequestBody CreateOrderRequest request) {

        log.info("Received request to create order: {}", request);
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create bulk orders", description = "Creates multiple orders with custom span tracking for each order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bulk orders created successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock for one or more products")
    })
    public ResponseEntity<List<OrderResponse>> createBulkOrders(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Bulk order creation request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"orders\": [{\"productId\": 1, \"quantity\": 2}, {\"productId\": 2, \"quantity\": 1}]}"))) @RequestBody CreateBulkOrderRequest request) {

        log.info("Received request to create bulk orders with {} items", request.getOrders().size());
        List<OrderResponse> responses = orderService.createBulkOrders(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieves order details by order ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {

        log.info("Received request to get order with id: {}", orderId);
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }
}
