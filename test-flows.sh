#!/bin/bash
# Test script for distributed tracing flows (Linux/Mac)
# This script tests all 5 flows with user confirmation between each

set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo "========================================"
echo "Distributed Tracing Test Flows"
echo "========================================"
echo ""
echo "Prerequisites:"
echo "- All services must be running (docker-compose up)"
echo "- curl must be installed"
echo ""
echo "This script will test 5 flows:"
echo "1. Create Order (Happy Path)"
echo "2. Create Order (Out of Stock)"
echo "3. Get Order Details"
echo "4. Simulated Slow Operation"
echo "5. Bulk Order Creation"
echo ""
echo "After each flow, you can:"
echo "- Press ENTER to continue to next flow"
echo "- Press Ctrl+C to exit"
echo ""
read -p "Press ENTER to start..."

# ========================================
# Flow 1: Create Order (Happy Path)
# ========================================
echo ""
echo "========================================"
echo -e "${BLUE}FLOW 1: Create Order (Happy Path)${NC}"
echo "========================================"
echo ""
echo "Testing: POST /api/orders with productId=1, quantity=2"
echo "Expected: 201 Created with order details"
echo ""

curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}' \
  -w "\n\nHTTP Status: %{http_code}\n"

echo ""
echo "What to check in Jaeger UI (http://localhost:16686):"
echo "- Service: api-gateway"
echo "- Look for trace with 6-7 spans"
echo "- All spans should be green (status: OK)"
echo "- Check attributes: order.product_id, order.quantity"
echo ""
read -p "Press ENTER to continue to Flow 2..."

# ========================================
# Flow 2: Create Order (Out of Stock)
# ========================================
echo ""
echo "========================================"
echo -e "${RED}FLOW 2: Create Order (Out of Stock)${NC}"
echo "========================================"
echo ""
echo "Testing: POST /api/orders with productId=3, quantity=100"
echo "Expected: 400 Bad Request with insufficient stock error"
echo ""

curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 3, "quantity": 100}' \
  -w "\n\nHTTP Status: %{http_code}\n"

echo ""
echo "What to check in Jaeger UI:"
echo "- Look for trace with RED error indicators"
echo "- Error message should show: \"Insufficient stock\""
echo "- Error propagates from inventory-service to api-gateway"
echo ""
read -p "Press ENTER to continue to Flow 3..."

# ========================================
# Flow 3: Get Order Details
# ========================================
echo ""
echo "========================================"
echo -e "${GREEN}FLOW 3: Get Order Details${NC}"
echo "========================================"
echo ""
echo "Testing: GET /api/orders/1"
echo "Expected: 200 OK with order details"
echo ""

curl http://localhost:8080/api/orders/1 \
  -w "\n\nHTTP Status: %{http_code}\n"

echo ""
echo "What to check in Jaeger UI:"
echo "- Simple trace with 3 spans (linear, no branching)"
echo "- Fast execution (no external service calls)"
echo "- Database query span visible"
echo ""
read -p "Press ENTER to continue to Flow 4..."

# ========================================
# Flow 4: Simulated Slow Operation
# ========================================
echo ""
echo "========================================"
echo -e "${YELLOW}FLOW 4: Simulated Slow Operation${NC}"
echo "========================================"
echo ""
echo "Testing: GET /api/inventory/products"
echo "Expected: 200 OK after ~2.5 second delay"
echo "Note: This will take a few seconds..."
echo ""

curl http://localhost:8080/api/inventory/products \
  -w "\n\nHTTP Status: %{http_code}\n"

echo ""
echo "What to check in Jaeger UI:"
echo "- Total trace duration: ~2.5+ seconds"
echo "- Database query span shows 2500ms+ duration"
echo "- Visual timeline clearly shows the bottleneck"
echo ""
read -p "Press ENTER to continue to Flow 5..."

# ========================================
# Flow 5: Bulk Order Creation
# ========================================
echo ""
echo "========================================"
echo -e "${MAGENTA}FLOW 5: Bulk Order Creation (Custom Spans)${NC}"
echo "========================================"
echo ""
echo "Testing: POST /api/orders/bulk with 3 orders"
echo "Expected: 201 Created with array of orders"
echo ""

curl -X POST http://localhost:8080/api/orders/bulk \
  -H "Content-Type: application/json" \
  -d '{"orders": [{"productId": 1, "quantity": 2}, {"productId": 2, "quantity": 1}, {"productId": 4, "quantity": 3}]}' \
  -w "\n\nHTTP Status: %{http_code}\n"

echo ""
echo "What to check in Jaeger UI:"
echo "- Look for CUSTOM spans named \"process-single-order\""
echo "- 20+ spans total (3 custom spans + nested operations)"
echo "- Each custom span has attributes: order.index, order.product_id"
echo "- Events: processing-started, stock-validated, order-persisted"
echo ""
echo "========================================"
echo -e "${CYAN}ALL FLOWS COMPLETED!${NC}"
echo "========================================"
echo ""
echo "Next steps:"
echo "1. Open Jaeger UI: http://localhost:16686"
echo "2. Select service: api-gateway"
echo "3. Click \"Find Traces\""
echo "4. Examine each trace for the flows above"
echo ""
read -p "Press any key to exit..."
