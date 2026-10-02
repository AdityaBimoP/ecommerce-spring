package com.superstyleshop.service;

import com.superstyleshop.model.Product;
import com.superstyleshop.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminManagementService {
    private final ProductRepository products;

    public AdminManagementService(ProductRepository products) {
        this.products = products;
    }

    @Transactional
    public void updateProduct(Long productId, BigDecimal price, int stockQuantity) {
        Product product = products.findByIdForUpdate(Objects.requireNonNull(productId))
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        product.updateInventory(price, stockQuantity);
        products.save(product);
    }
}
