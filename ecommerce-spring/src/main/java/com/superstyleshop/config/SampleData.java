package com.superstyleshop.config;

import com.superstyleshop.model.Product;
import com.superstyleshop.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleData {
    @Bean
    CommandLineRunner seedProducts(ProductRepository products) {
        return args -> {
            if (products.count() != 0) {
                return;
            }
            products.saveAll(Objects.requireNonNull(List.of(
                    new Product("Wireless Headphones",
                            "High-quality wireless headphones with noise cancellation.",
                            new BigDecimal("99.99"),
                            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=85",
                            "Electronics"),
                    new Product("Smart Watch",
                            "Track your fitness and notifications on the go.",
                            new BigDecimal("149.99"),
                            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=85",
                            "Electronics"),
                    new Product("Classic T-Shirt",
                            "Comfortable cotton t-shirt in various sizes.",
                            new BigDecimal("19.99"),
                            "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=85",
                            "Clothing"),
                    new Product("Denim Jeans",
                            "Stylish denim jeans for everyday wear.",
                            new BigDecimal("39.99"),
                            "https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=900&q=85",
                            "Clothing"),
                    new Product("Bestseller Novel",
                            "A gripping story from a bestselling author.",
                            new BigDecimal("12.99"),
                            "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=900&q=85",
                            "Books"),
                    new Product("Cookbook",
                            "Delicious recipes from around the world.",
                            new BigDecimal("24.99"),
                            "https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=85",
                            "Books"))));
        };
    }
}
