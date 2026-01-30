package com.example.order.service;

import com.example.order.client.InventoryClient;
import com.example.order.dto.CreateBulkOrderRequest;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.dto.ProductResponse;
import com.example.order.entity.Order;
import com.example.order.entity.OrderStatus;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.repository.OrderRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final Tracer tracer;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for product {} with quantity {}", request.getProductId(), request.getQuantity());

        // Check stock availability via Inventory Service
        ProductResponse product = inventoryClient.checkStock(request.getProductId(), request.getQuantity());

        // Add custom span attributes
        Span currentSpan = Span.current();
        currentSpan.setAttribute("order.product_id", request.getProductId());
        currentSpan.setAttribute("order.quantity", request.getQuantity());

        // Create order
        Order order = Order.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(OrderStatus.CREATED)
                .build();

        order = orderRepository.save(order);

        // Add span event and attribute after order creation
        currentSpan.addEvent("order-created");
        currentSpan.setAttribute("order.id", order.getId());

        log.info("Order created successfully with id: {}", order.getId());

        return mapToResponse(order);
    }

    @Transactional
    public List<OrderResponse> createBulkOrders(CreateBulkOrderRequest request) {
        log.info("Creating bulk orders for {} items", request.getOrders().size());

        List<OrderResponse> responses = new ArrayList<>();

        // Process each order with a custom span
        for (int i = 0; i < request.getOrders().size(); i++) {
            CreateOrderRequest orderRequest = request.getOrders().get(i);

            // Create custom manual span for each order processing
            Span span = tracer.spanBuilder("process-single-order")
                    .setAttribute("order.index", i)
                    .setAttribute("order.product_id", orderRequest.getProductId())
                    .setAttribute("order.quantity", orderRequest.getQuantity())
                    .startSpan();

            try (Scope scope = span.makeCurrent()) {
                log.info("Processing order {} of {}: product={}, quantity={}",
                        i + 1, request.getOrders().size(),
                        orderRequest.getProductId(), orderRequest.getQuantity());

                span.addEvent("processing-started");

                // Check stock
                ProductResponse product = inventoryClient.checkStock(
                        orderRequest.getProductId(),
                        orderRequest.getQuantity());

                span.addEvent("stock-validated");

                // Create order
                Order order = Order.builder()
                        .productId(orderRequest.getProductId())
                        .quantity(orderRequest.getQuantity())
                        .status(OrderStatus.CREATED)
                        .build();

                order = orderRepository.save(order);

                span.setAttribute("order.id", order.getId());
                span.addEvent("order-persisted");

                responses.add(mapToResponse(order));

                log.info("Successfully processed order {} with id: {}", i + 1, order.getId());

            } catch (Exception e) {
                span.recordException(e);
                span.setAttribute("error", true);
                log.error("Failed to process order {} of {}", i + 1, request.getOrders().size(), e);
                throw e;
            } finally {
                span.end();
            }
        }

        log.info("Bulk order creation completed. Created {} orders", responses.size());
        return responses;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        log.info("Fetching order with id: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        String.format("Order with id %d not found", orderId)));

        // Add custom span attribute
        Span.current().setAttribute("order.id", orderId);

        log.info("Order found: {}", orderId);
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
