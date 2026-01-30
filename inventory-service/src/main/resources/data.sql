-- Initialize inventory with sample products
INSERT INTO products (name, stock_quantity, price) VALUES
('Laptop', 10, 999.99),
('Mouse', 50, 29.99),
('Keyboard', 5, 79.99),
('Monitor', 100, 299.99),
('Headphones', 20, 149.99)
ON CONFLICT DO NOTHING;
