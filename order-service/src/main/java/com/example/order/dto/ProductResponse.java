package com.example.order.dto;

import java.math.BigDecimal;

public class ProductResponse {
    private Long id;
    private String name;
    private Integer stockQuantity;
    private BigDecimal price;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, Integer stockQuantity, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
        this.price = price;
    }
    
    // Builder pattern implementation
    public static class ProductResponseBuilder {
        private Long id;
        private String name;
        private Integer stockQuantity;
        private BigDecimal price;

        ProductResponseBuilder() {
        }

        public ProductResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProductResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProductResponseBuilder stockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
            return this;
        }

        public ProductResponseBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ProductResponse build() {
            return new ProductResponse(id, name, stockQuantity, price);
        }
    }

    public static ProductResponseBuilder builder() {
        return new ProductResponseBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
