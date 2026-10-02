package com.superstyleshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String imageUrl;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, columnDefinition = "integer default 99")
    private int stockQuantity = 99;

    protected Product() {
    }

    public Product(String name, String description, BigDecimal price, String imageUrl, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void updateInventory(BigDecimal price, int stockQuantity) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity < 1 || quantity > stockQuantity) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock.");
        }
        stockQuantity -= quantity;
    }
}
