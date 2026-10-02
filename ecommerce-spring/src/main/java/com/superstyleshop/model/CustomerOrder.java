package com.superstyleshop.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(nullable = false)
    private int orderNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status = "PAID_DEMO";

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderLine> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(UserAccount user, String paymentMethod, int orderNumber) {
        this.user = user;
        this.paymentMethod = paymentMethod;
        this.orderNumber = orderNumber;
        this.totalAmount = BigDecimal.ZERO;
    }

    public void addItem(Product product, int quantity) {
        items.add(new OrderLine(this, product, quantity, product.getPrice()));
        totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    public Long getId() {
        return id;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void updateItem(Long itemId, int quantity, BigDecimal unitPrice) {
        OrderLine item = items.stream()
                .filter(orderLine -> orderLine.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order item not found."));
        item.update(quantity, unitPrice);
        recalculateTotal();
    }

    private void recalculateTotal() {
        BigDecimal updatedTotal = BigDecimal.ZERO;
        for (OrderLine item : items) {
            updatedTotal = updatedTotal.add(Objects.requireNonNull(item).getLineTotal());
        }
        totalAmount = updatedTotal;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderLine> getItems() {
        return List.copyOf(items);
    }
}
