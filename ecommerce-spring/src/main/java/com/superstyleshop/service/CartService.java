package com.superstyleshop.service;

import com.superstyleshop.model.Product;
import com.superstyleshop.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class CartService {
    private static final String PRODUCT_NOT_FOUND = "Product not found.";

    private final ProductRepository products;
    private final Map<Long, Integer> quantities = new LinkedHashMap<>();

    public CartService(ProductRepository products) {
        this.products = products;
    }

    public List<CartLine> getItems() {
        List<CartLine> items = new ArrayList<>();
        quantities.forEach((productId, quantity) -> products.findById(Objects.requireNonNull(productId))
                .ifPresent(product -> items.add(new CartLine(product, quantity))));
        return List.copyOf(items);
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartLine item : getItems()) {
            total = total.add(Objects.requireNonNull(item).getLineTotal());
        }
        return total;
    }

    public int getItemCount() {
        int itemCount = 0;
        for (Integer quantity : quantities.values()) {
            itemCount += Objects.requireNonNull(quantity);
        }
        return itemCount;
    }

    public void add(Long productId, int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99.");
        }
        Product product = products.findById(Objects.requireNonNull(productId))
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        int currentQuantity = quantities.getOrDefault(productId, 0);
        if (currentQuantity + quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock.");
        }
        quantities.merge(productId, quantity, (current, added) -> {
            if (current + added > 99) {
                throw new IllegalArgumentException("A maximum of 99 units per product is allowed.");
            }
            return current + added;
        });
    }

    public void buyNow(Long productId) {
        Product product = products.findById(Objects.requireNonNull(productId))
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        if (product.getStockQuantity() < 1) {
            throw new IllegalArgumentException("This product is out of stock.");
        }
        quantities.clear();
        quantities.put(productId, 1);
    }

    public void update(Long productId, int quantity) {
        if (!quantities.containsKey(productId)) {
            throw new IllegalArgumentException("That product is not in your cart.");
        }
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99.");
        }
        Product product = products.findById(Objects.requireNonNull(productId))
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock.");
        }
        quantities.put(productId, quantity);
    }

    public void remove(Long productId) {
        quantities.remove(productId);
    }

    public boolean isEmpty() {
        return quantities.isEmpty();
    }

    public void clear() {
        quantities.clear();
    }

    public record CartLine(Product product, int quantity) {
        public BigDecimal getLineTotal() {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}
