package com.example.gateway.controller;

import com.example.gateway.service.InventoryProxyService;
import com.example.gateway.service.OrderProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);
    private final OrderProxyService orderProxyService;
    private final InventoryProxyService inventoryProxyService;

    public GatewayController(OrderProxyService orderProxyService, InventoryProxyService inventoryProxyService) {
        this.orderProxyService = orderProxyService;
        this.inventoryProxyService = inventoryProxyService;
    }


    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody String requestBody) {

        log.info("Received order creation request at gateway");
        return orderProxyService.createOrder(requestBody);
    }

    @PostMapping("/orders/bulk")
    public ResponseEntity<String> createBulkOrders(@RequestBody String requestBody) {

        log.info("Received bulk order creation request at gateway");
        return orderProxyService.createBulkOrders(requestBody);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<String> getOrder(@PathVariable Long orderId) {

        log.info("Received get order request at gateway for order id: {}", orderId);
        return orderProxyService.getOrder(orderId);
    }

    @GetMapping("/inventory/products")
    public ResponseEntity<String> getAllProducts() {
        log.info("Received get all products request at gateway");
        return inventoryProxyService.getAllProducts();
    }
}
