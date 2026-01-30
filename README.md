# Learning Distributed Tracing using Jaeger with OpenTelemetry

A comprehensive educational project demonstrating distributed tracing concepts in a microservices architecture using **Spring Boot 4.0**, **OpenTelemetry**, and **Jaeger**.

## What This Project Teaches

This project is designed to teach you the fundamentals of distributed tracing:

- **What is distributed tracing?** A method of tracking requests as they flow through multiple microservices
- **Why it matters:** Essential for debugging, performance optimization, and understanding system behavior in microservices architectures
- **How to implement it:** Using OpenTelemetry (vendor-neutral instrumentation) and Jaeger (tracing backend)
- **Key concepts:** Traces, spans, context propagation, sampling, span attributes, and log correlation

### Technologies Used

- **Spring Boot 4.0.2** with native `spring-boot-starter-opentelemetry`
- **Java 21** with modern features (records, pattern matching)
- **Gradle** as the build tool
- **OpenTelemetry** for instrumentation (OTLP protocol)
- **Jaeger** as the tracing backend
- **PostgreSQL** for realistic database tracing
- **Docker Compose** for local orchestration

### What This Project Does NOT Include

This is a focused learning project. It explicitly excludes:

- ❌ Authentication/Authorization (Spring Security)
- ❌ Message queues (Kafka, RabbitMQ)
- ❌ Metrics dashboards (Prometheus/Grafana)
- ❌ Service discovery (Eureka/Consul)
- ❌ API Gateway patterns (rate limiting, circuit breakers)
- ❌ Kubernetes deployment
- ❌ CI/CD pipelines

**Focus:** Distributed tracing concepts only.

## Architecture

This project consists of 3 microservices in an e-commerce domain:

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  API Gateway    │  (Port 8080)
│  No Database    │
└────────┬────────┘
         │
         ├──────────────────┬────────────────────┐
         ▼                  ▼                    ▼
┌─────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  Order Service  │  │ Inventory Service│  │     Jaeger       │
│  Port 8081      │──│  Port 8082       │  │   Port 16686     │
│  PostgreSQL     │  │  PostgreSQL      │  │  (Tracing UI)    │
│  (orders_db)    │  │  (inventory_db)  │  └──────────────────┘
└─────────────────┘  └──────────────────┘
```

### Service Responsibilities

1. **API Gateway** (Port 8080)
   - Entry point for all client requests
   - Routes requests to downstream services
   - No database dependency

2. **Order Service** (Port 8081)
   - Manages order creation and retrieval
   - Calls Inventory Service to validate stock
   - PostgreSQL database: `orders` table

3. **Inventory Service** (Port 8082)
   - Manages product inventory
   - PostgreSQL database: `products` table
   - Pre-loaded with 5 sample products

## Quick Start

### Prerequisites

- **Docker & Docker Compose** (required)
- **Java 21** (optional, only for local development)
- **Gradle** (optional, wrapper included)

### Running the Project

```bash
# Clone the repository
git clone <your-repo-url>
cd learning-distributed-tracing

# Build and run all services with Docker Compose
docker-compose up --build

# Wait for all services to be healthy (check logs)
# You should see "Started [ServiceName]Application" for each service
```

### Access Points

Once all services are running:

- **API Gateway Swagger UI:** http://localhost:8080/swagger-ui.html
- **Order Service Swagger UI:** http://localhost:8081/swagger-ui.html
- **Inventory Service Swagger UI:** http://localhost:8082/swagger-ui.html
- **Jaeger UI:** http://localhost:16686

## API Endpoints & Trace Scenarios

This project demonstrates 5 key distributed tracing scenarios. Each scenario teaches different tracing concepts.

---

### Flow 1: Create Order (Happy Path)

**Purpose:** Demonstrates successful multi-service trace with database operations

**Concepts Learned:**
- Multi-service trace propagation
- Parent-child span relationships
- Database query spans
- Span attributes and events

**Request:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}'
```

**Expected Response:** `201 Created`
```json
{
  "orderId": 1,
  "productId": 1,
  "quantity": 2,
  "status": "CREATED",
  "createdAt": "2026-01-28T22:30:00"
}
```

**What to Observe in Jaeger UI:**

1. Open Jaeger UI: http://localhost:16686
2. Select **Service:** `api-gateway`
3. Click **Find Traces**
4. Click on the most recent trace

**Expected Trace Structure:**
```
api-gateway: POST /api/orders (root span)
├── order-service: POST /api/orders
    ├── order-service: GET http://inventory-service:8082/api/inventory/1
    │   └── inventory-service: GET /api/inventory/{productId}
    │       └── inventory-service: SELECT products (database query)
    └── order-service: INSERT orders (database query)
```

**Span Count:** 6-7 spans

**Key Observations:**
- All spans should have **status: OK** (green)
- Check **Tags/Attributes:**
  - `order.product_id: 1`
  - `order.quantity: 2`
  - `product.name: Laptop`
  - `stock.available: 10`
  - `stock.requested: 2`
- Check **Events:**
  - `stock-checked` (in inventory-service span)
  - `order-created` (in order-service span)
- **Trace ID** is consistent across all spans
- **Parent-child relationships** show proper nesting

---

### Flow 2: Create Order (Out of Stock)

**Purpose:** Demonstrates error propagation across services

**Concepts Learned:**
- Error status on spans
- Exception recording
- Error propagation through service boundaries

**Request:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 3, "quantity": 100}'
```

**Expected Response:** `400 Bad Request`
```json
{
  "timestamp": "2026-01-28T22:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient stock for product 3. Requested: 100, Available: 5"
}
```

**What to Observe in Jaeger UI:**

1. Search for service: `api-gateway`
2. Find the trace with **red error indicator**
3. Click to view details

**Expected Trace Structure:**
```
api-gateway: POST /api/orders (ERROR)
└── order-service: POST /api/orders (ERROR)
    └── order-service: GET http://inventory-service:8082/api/inventory/3 (ERROR)
        └── inventory-service: GET /api/inventory/{productId} (ERROR)
            └── inventory-service: SELECT products (OK)
```

**Key Observations:**
- Multiple spans marked with **error status** (red)
- Error message visible in span details
- Database query succeeds, but business logic fails
- Error propagates from inventory-service → order-service → api-gateway
- HTTP status code: 400 in span tags

---

### Flow 3: Get Order Details

**Purpose:** Demonstrates simple database query tracing

**Concepts Learned:**
- Read-only operations
- Simple trace structure
- Database query optimization insights

**Request:**
```bash
# First create an order (Flow 1), then get it
curl http://localhost:8080/api/orders/1
```

**Expected Response:** `200 OK`
```json
{
  "orderId": 1,
  "productId": 1,
  "quantity": 2,
  "status": "CREATED",
  "createdAt": "2026-01-28T22:30:00"
}
```

**What to Observe in Jaeger UI:**

**Expected Trace Structure:**
```
api-gateway: GET /api/orders/{orderId}
└── order-service: GET /api/orders/{orderId}
    └── order-service: SELECT orders (database query)
```

**Span Count:** 3 spans

**Key Observations:**
- Simple, linear trace (no branching)
- Database query span shows SQL execution time
- Span attribute: `order.id: 1`
- Fast execution (no external service calls)

---

### Flow 4: Simulated Slow Operation

**Purpose:** Demonstrates latency bottleneck identification

**Concepts Learned:**
- Performance profiling with traces
- Identifying slow operations
- Database query latency

**Request:**
```bash
curl http://localhost:8080/api/inventory/products
```

**Expected Response:** `200 OK` (after ~2.5 second delay)
```json
[
  {"id": 1, "name": "Laptop", "stockQuantity": 10, "price": 999.99},
  {"id": 2, "name": "Mouse", "stockQuantity": 50, "price": 29.99},
  ...
]
```

**What to Observe in Jaeger UI:**

**Expected Trace Structure:**
```
api-gateway: GET /api/inventory/products
└── inventory-service: GET /api/inventory/products
    └── inventory-service: SELECT products (SLOW - ~2.5s)
```

**Key Observations:**
- **Total trace duration:** ~2.5+ seconds
- The database query span shows **2500ms+ duration**
- Visual timeline clearly shows the bottleneck
- This simulates a slow database query (intentional `Thread.sleep(2500)`)
- In production, this would help identify actual slow queries

**Learning Point:** Traces make it easy to identify which specific operation is causing latency.

---

### Flow 5: Custom Business Logic with Manual Spans

**Purpose:** Demonstrates manual span creation for business logic

**Concepts Learned:**
- Creating custom spans programmatically
- Span attributes for business context
- Span events for important milestones
- Exception recording in custom spans

**Request:**
```bash
curl -X POST http://localhost:8080/api/orders/bulk \
  -H "Content-Type: application/json" \
  -d '{
    "orders": [
      {"productId": 1, "quantity": 2},
      {"productId": 2, "quantity": 1},
      {"productId": 4, "quantity": 3}
    ]
  }'
```

**Expected Response:** `201 Created`
```json
[
  {"orderId": 2, "productId": 1, "quantity": 2, "status": "CREATED", ...},
  {"orderId": 3, "productId": 2, "quantity": 1, "status": "CREATED", ...},
  {"orderId": 4, "productId": 4, "quantity": 3, "status": "CREATED", ...}
]
```

**What to Observe in Jaeger UI:**

**Expected Trace Structure:**
```
api-gateway: POST /api/orders/bulk
└── order-service: POST /api/orders/bulk
    ├── order-service: process-single-order (index=0) [CUSTOM SPAN]
    │   ├── order-service: GET http://inventory-service:8082/api/inventory/1
    │   │   └── inventory-service: GET /api/inventory/{productId}
    │   │       └── inventory-service: SELECT products
    │   └── order-service: INSERT orders
    ├── order-service: process-single-order (index=1) [CUSTOM SPAN]
    │   ├── order-service: GET http://inventory-service:8082/api/inventory/2
    │   │   └── inventory-service: GET /api/inventory/{productId}
    │   │       └── inventory-service: SELECT products
    │   └── order-service: INSERT orders
    └── order-service: process-single-order (index=2) [CUSTOM SPAN]
        ├── order-service: GET http://inventory-service:8082/api/inventory/4
        │   └── inventory-service: GET /api/inventory/{productId}
        │       └── inventory-service: SELECT products
        └── order-service: INSERT orders
```

**Span Count:** 20+ spans (3 custom spans + nested operations)

**Key Observations:**
- **Custom span name:** `process-single-order` (created manually in code)
- Each custom span has attributes:
  - `order.index: 0, 1, 2`
  - `order.product_id: 1, 2, 4`
  - `order.quantity: 2, 1, 3`
- Each custom span has events:
  - `processing-started`
  - `stock-validated`
  - `order-persisted`
- Sequential processing visible in timeline
- Each iteration is clearly isolated in its own span

**Code Reference:** See `OrderService.createBulkOrders()` method for manual span creation using `Tracer.spanBuilder()`.

---

## Key Concepts Explained

### Traces
A **trace** represents the complete journey of a request through your system. It's composed of one or more spans.

- **Trace ID:** Unique identifier for the entire request flow (e.g., `abc123def456`)
- **Duration:** Total time from first span start to last span end
- **Services:** All services involved in handling the request

### Spans
A **span** represents a single unit of work within a trace (e.g., HTTP request, database query, business logic).

- **Span ID:** Unique identifier for this specific operation
- **Parent Span ID:** Links to the parent span (creates hierarchy)
- **Operation Name:** Describes what the span represents (e.g., `GET /api/orders`)
- **Duration:** How long this operation took
- **Status:** OK, ERROR, or UNSET

### Parent-Child Relationships
Spans form a tree structure:
- **Root span:** The first span (no parent) - typically the API Gateway request
- **Child spans:** Operations triggered by the parent (e.g., downstream service calls)
- **Siblings:** Multiple child spans from the same parent (e.g., parallel operations)

### Context Propagation
How does Jaeger know which spans belong to the same trace?

**W3C Trace Context Headers:**
- `traceparent: 00-{trace-id}-{span-id}-{flags}`
- Automatically added to HTTP requests by OpenTelemetry
- Propagates trace context across service boundaries

**Example:**
```
Client → API Gateway (trace-id: abc123)
         ↓ (HTTP header: traceparent: 00-abc123-span1-01)
         Order Service (same trace-id: abc123)
         ↓ (HTTP header: traceparent: 00-abc123-span2-01)
         Inventory Service (same trace-id: abc123)
```

### Sampling
**Sampling rate** determines what percentage of traces to record.

- **100% sampling (this project):** Record every single trace
  - Good for: Development, debugging, learning
  - Bad for: High-traffic production (too much data)
- **Production sampling:** Typically 1-10%
  - Reduces storage and performance overhead
  - Still captures representative sample

**Configuration:** `management.tracing.sampling.probability: 1.0` (100%)

### OTLP (OpenTelemetry Protocol)
**Why OTLP instead of Jaeger-specific SDK?**

- **Vendor-neutral:** Switch backends without code changes
- **Future-proof:** OpenTelemetry is the industry standard
- **Flexibility:** Works with Jaeger, Zipkin, Tempo, SigNoz, cloud providers

**How it works:**
```
Your App → OpenTelemetry SDK → OTLP Exporter → Jaeger Collector
                                              → Tempo
                                              → SigNoz
                                              → AWS X-Ray
```

### Span Attributes
**Tags** that add context to spans (key-value pairs).

**Examples:**
- `http.method: POST`
- `http.status_code: 201`
- `db.system: postgresql`
- `order.id: 123` (custom attribute)

**Code example:**
```java
Span.current().setAttribute("order.id", orderId);
```

### Span Events
**Timestamped events** within a span (like breadcrumbs).

**Examples:**
- `stock-checked`
- `order-created`
- `payment-processed`

**Code example:**
```java
span.addEvent("stock-validated");
```

### Span Status
Indicates whether the operation succeeded or failed.

- **OK:** Operation completed successfully
- **ERROR:** Operation failed (exception thrown, HTTP 4xx/5xx)
- **UNSET:** Status not explicitly set

---

## Log Correlation

**What is log correlation?**

Linking log messages to distributed traces by including `trace_id` and `span_id` in logs.

**Why it matters:**
- See a suspicious log message → Copy trace_id → Find the complete distributed trace in Jaeger
- Correlate logs from multiple services for the same request

### Example Log Output

```
2026-01-28 22:30:15.123 INFO [api-gateway,abc123def456,span789xyz] - Received order request
2026-01-28 22:30:15.456 INFO [order-service,abc123def456,span111aaa] - Creating order for product 1
2026-01-28 22:30:15.789 INFO [inventory-service,abc123def456,span222bbb] - Checking stock for product 1
```

**Format:** `[service-name,trace_id,span_id]`

### How to Use Log Correlation

1. **Find a log message** with a trace_id (e.g., `abc123def456`)
2. **Copy the trace_id**
3. **Open Jaeger UI:** http://localhost:16686
4. **Paste trace_id** in the search box
5. **View the complete trace** with all services and operations

### Configuration

Log correlation is configured in `application.yml`:

```yaml
logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

OpenTelemetry automatically populates the MDC (Mapped Diagnostic Context) with trace context.

---

## Manual Span Creation

**When to create custom spans:**
- Business logic that isn't automatically instrumented
- Loops processing multiple items (see Flow 5)
- Complex algorithms you want to profile
- Important business milestones

### Code Example (from OrderService.java)

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final Tracer tracer;
    
    public void processBulkOrders(List<OrderRequest> orders) {
        for (int i = 0; i < orders.size(); i++) {
            // Create custom span
            Span span = tracer.spanBuilder("process-single-order")
                    .setAttribute("order.index", i)
                    .setAttribute("order.product_id", orders.get(i).getProductId())
                    .startSpan();
            
            try (Scope scope = span.makeCurrent()) {
                // Add events
                span.addEvent("processing-started");
                
                // Your business logic here
                processOrder(orders.get(i));
                
                span.addEvent("order-persisted");
            } catch (Exception e) {
                span.recordException(e);
                span.setAttribute("error", true);
                throw e;
            } finally {
                span.end();
            }
        }
    }
}
```

### Best Practices

- **Meaningful names:** Use descriptive span names (e.g., `process-single-order`, not `loop-iteration`)
- **Always end spans:** Use try-finally or try-with-resources
- **Add context:** Use attributes and events generously
- **Record exceptions:** Use `span.recordException(e)` for proper error tracking

---

## Troubleshooting

### Traces Not Appearing in Jaeger

**Symptoms:** No traces visible in Jaeger UI after making requests

**Solutions:**

1. **Check Jaeger is running:**
   ```bash
   docker ps | grep jaeger
   ```
   Should show `jaegertracing/all-in-one` container

2. **Check service logs for OTLP export errors:**
   ```bash
   docker-compose logs order-service | grep -i "otlp\|trace"
   ```

3. **Verify OTLP endpoint configuration:**
   - Check `application.yml`: `management.otlp.tracing.endpoint`
   - Should be `http://jaeger:4318/v1/traces` in Docker

4. **Check network connectivity:**
   ```bash
   docker-compose exec order-service ping jaeger
   ```

5. **Verify sampling is enabled:**
   - Check `management.tracing.sampling.probability: 1.0`

### Services Not Starting

**Symptoms:** Containers exit or restart repeatedly

**Solutions:**

1. **Check Docker logs:**
   ```bash
   docker-compose logs <service-name>
   ```

2. **Ensure PostgreSQL is healthy:**
   ```bash
   docker-compose ps postgres
   ```
   Status should be "Up (healthy)"

3. **Check port conflicts:**
   ```bash
   # Windows
   netstat -ano | findstr "8080\|8081\|8082\|5432\|16686"
   ```
   Kill processes using these ports or change ports in `docker-compose.yml`

4. **Rebuild containers:**
   ```bash
   docker-compose down -v
   docker-compose up --build
   ```

### Database Connection Errors

**Symptoms:** Services fail with "Connection refused" or "Unknown database"

**Solutions:**

1. **Verify PostgreSQL is running:**
   ```bash
   docker-compose ps postgres
   ```

2. **Check database initialization:**
   ```bash
   docker-compose exec postgres psql -U postgres -c "\l"
   ```
   Should show `orders_db` and `inventory_db`

3. **Verify credentials in application.yml:**
   - Username: `postgres`
   - Password: `postgres`

4. **Check service dependencies:**
   - Services should wait for `postgres: condition: service_healthy`

### Changing Log Levels at Runtime

**Use Spring Boot Actuator to change log levels without restarting:**

```bash
# View current log level
curl http://localhost:8081/actuator/loggers/com.example.order

# Change to DEBUG
curl -X POST http://localhost:8081/actuator/loggers/com.example.order \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel":"DEBUG"}'

# Change to TRACE (very verbose)
curl -X POST http://localhost:8081/actuator/loggers/com.example.order \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel":"TRACE"}'

# Reset to INFO
curl -X POST http://localhost:8081/actuator/loggers/com.example.order \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel":"INFO"}'
```

---

## How to Replace Jaeger (Backend Independence)

One of the key benefits of using OpenTelemetry is **backend independence**. You can switch from Jaeger to any OTLP-compatible backend with minimal configuration changes.

### Switching Backends

**No code changes required!** Only configuration changes in `application.yml`:

#### For Grafana Tempo

```yaml
management:
  otlp:
    tracing:
      endpoint: http://tempo:4318/v1/traces
```

#### For SigNoz

```yaml
management:
  otlp:
    tracing:
      endpoint: http://signoz:4318/v1/traces
```

#### For Zipkin (with OTLP support)

```yaml
management:
  otlp:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

#### For Cloud Providers

**AWS X-Ray:**
```yaml
management:
  otlp:
    tracing:
      endpoint: https://xray.us-east-1.amazonaws.com
```

**Google Cloud Trace:**
```yaml
management:
  otlp:
    tracing:
      endpoint: https://cloudtrace.googleapis.com/v1/projects/YOUR_PROJECT/traces
```

**Azure Monitor:**
```yaml
management:
  otlp:
    tracing:
      endpoint: https://YOUR_RESOURCE.monitor.azure.com
```

### Other OTLP-Compatible Backends

- **Grafana Tempo** - Scalable, cost-effective tracing backend
- **SigNoz** - Open-source observability platform (traces + metrics + logs)
- **Zipkin** - Lightweight distributed tracing system
- **Elastic APM** - Part of Elastic Stack
- **Honeycomb** - Observability platform for production systems
- **Lightstep** - Enterprise observability
- **New Relic** - Full-stack observability
- **Datadog APM** - Application performance monitoring

### Update Docker Compose

Replace the `jaeger` service in `docker-compose.yml` with your chosen backend. Example for Grafana Tempo:

```yaml
tempo:
  image: grafana/tempo:latest
  ports:
    - "4318:4318"  # OTLP HTTP
  # ... tempo configuration
```

Then update environment variables:
```yaml
environment:
  - OTEL_EXPORTER_OTLP_ENDPOINT=http://tempo:4318
```

**That's it!** Your instrumentation code remains unchanged.

---

## Learning Path

Suggested order to explore this project:

1. **Start the system:**
   ```bash
   docker-compose up --build
   ```

2. **Trigger Flow 1 (happy path):**
   - Make the API call
   - Open Jaeger UI
   - Examine the trace structure

3. **Read Order Service code:**
   - `OrderService.java` - See auto-instrumentation
   - `InventoryClient.java` - See HTTP client tracing
   - Notice you didn't write tracing code!

4. **Trigger Flow 5 (bulk orders):**
   - Find the custom `process-single-order` spans
   - See span attributes and events

5. **Examine OpenTelemetry configuration:**
   - `OpenTelemetryConfig.java` - Tracer bean
   - `application.yml` - OTLP configuration

6. **Trigger Flow 2 (out of stock):**
   - Observe error propagation
   - See how errors appear in traces

7. **Check log correlation:**
   ```bash
   docker-compose logs order-service
   ```
   - Copy a `trace_id` from logs
   - Search for it in Jaeger

8. **Experiment:**
   - Modify code to add more spans/attributes
   - Change sampling rate
   - Try different backends

9. **Trigger Flow 4 (slow operation):**
   - Identify the latency bottleneck
   - Understand performance profiling

10. **Replace Jaeger (optional):**
    - Try Grafana Tempo or SigNoz
    - See how easy it is to switch

---

## Additional Resources

### Official Documentation

- **OpenTelemetry:** https://opentelemetry.io/docs/
- **Jaeger:** https://www.jaegertracing.io/docs/
- **Spring Boot Observability:** https://spring.io/blog/2022/10/12/observability-with-spring-boot-3
- **Spring Boot 4 Release Notes:** https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes

### Concepts

- **Distributed Tracing Primer:** https://opentelemetry.io/docs/concepts/observability-primer/#distributed-tracing
- **W3C Trace Context:** https://www.w3.org/TR/trace-context/
- **OTLP Specification:** https://opentelemetry.io/docs/specs/otlp/

### Tutorials

- **OpenTelemetry Java:** https://opentelemetry.io/docs/languages/java/
- **Spring Boot + OpenTelemetry:** https://opentelemetry.io/docs/languages/java/automatic/spring-boot/

---

## Contributing & Feedback

This is a learning project created for educational purposes.

- **Suggestions welcome!** Open a GitHub issue with improvements
- **Found a bug?** Report it via GitHub issues
- **Want to add a feature?** Fork and experiment!

### Ideas for Extension

- Add more trace scenarios (parallel processing, retries, timeouts)
- Implement metrics with Micrometer
- Add log aggregation with ELK stack
- Create a frontend UI to visualize the e-commerce flow
- Add integration tests with trace validation

---

## License

This project is open-source and available for educational purposes.

---

## Summary

**What you learned:**
- ✅ How to instrument Spring Boot microservices with OpenTelemetry
- ✅ How to visualize distributed traces in Jaeger
- ✅ How to create custom spans for business logic
- ✅ How to correlate logs with traces
- ✅ How to identify performance bottlenecks
- ✅ How to track errors across services
- ✅ How to use vendor-neutral instrumentation (OTLP)

**Next steps:**
- Apply these concepts to your own projects
- Explore other OpenTelemetry features (metrics, logs)
- Try different tracing backends
- Learn about sampling strategies for production
- Investigate advanced topics (baggage, span links, trace exemplars)

Happy tracing! 
