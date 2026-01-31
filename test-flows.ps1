# Test all API endpoints using PowerShell
Write-Host "========================================" -ForegroundColor Green
Write-Host "Testing All API Endpoints" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Flow 1: Create Order (Happy Path)
Write-Host "Flow 1: Create Order (Happy Path)" -ForegroundColor Cyan
try {
    $body = '{"productId": 1, "quantity": 2}'
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Body $body -ContentType "application/json"
    Write-Host "SUCCESS: Created order $($response.orderId)" -ForegroundColor Green
}
catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Flow 2: Create Order (Out of Stock)
Write-Host "Flow 2: Create Order (Out of Stock - Expected Error)" -ForegroundColor Cyan
try {
    $body = '{"productId": 3, "quantity": 100}'
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/orders" -Method Post -Body $body -ContentType "application/json"
    Write-Host "UNEXPECTED: Should have failed" -ForegroundColor Red
}
catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "SUCCESS: Got expected 400 error (insufficient stock)" -ForegroundColor Green
    }
    else {
        Write-Host "FAILED: Unexpected error - $($_.Exception.Message)" -ForegroundColor Red
    }
}
Write-Host ""

# Flow 3: Get Order Details
Write-Host "Flow 3: Get Order Details" -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/1" -Method Get
    Write-Host "SUCCESS: Retrieved order $($response.orderId)" -ForegroundColor Green
}
catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Flow 4: Get All Products (Slow Operation)
Write-Host "Flow 4: Get All Products (Slow - ~2.5s delay)" -ForegroundColor Cyan
try {
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/inventory/products" -Method Get
    $stopwatch.Stop()
    Write-Host "SUCCESS: Retrieved $($response.Count) products in $($stopwatch.ElapsedMilliseconds)ms" -ForegroundColor Green
}
catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Flow 5: Bulk Order Creation
Write-Host "Flow 5: Bulk Order Creation" -ForegroundColor Cyan
try {
    $body = '{"orders": [{"productId": 1, "quantity": 2}, {"productId": 2, "quantity": 1}, {"productId": 4, "quantity": 3}]}'
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/orders/bulk" -Method Post -Body $body -ContentType "application/json"
    Write-Host "SUCCESS: Created $($response.Count) orders" -ForegroundColor Green
}
catch {
    Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "Testing Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
