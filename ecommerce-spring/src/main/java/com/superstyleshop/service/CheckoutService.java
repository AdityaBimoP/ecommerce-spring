package com.superstyleshop.service;

import com.superstyleshop.model.CustomerOrder;
import com.superstyleshop.model.Product;
import com.superstyleshop.model.UserAccount;
import com.superstyleshop.repository.CustomerOrderRepository;
import com.superstyleshop.repository.ProductRepository;
import com.superstyleshop.repository.UserAccountRepository;
import java.util.Objects;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {
    private final CustomerOrderRepository orders;
    private final ProductRepository products;
    private final UserAccountRepository users;

    public CheckoutService(
            CustomerOrderRepository orders,
            ProductRepository products,
            UserAccountRepository users) {
        this.orders = orders;
        this.products = products;
        this.users = users;
    }

    @Transactional
    public CustomerOrder placeOrder(
            CartService cart, UserAccount user, String paymentMethod) {
        if (user == null) {
            throw new IllegalArgumentException("Log in to your account before checkout.");
        }
        UserAccount purchasingUser = users.findByIdForUpdate(Objects.requireNonNull(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (!purchasingUser.isAdmin()
                && (purchasingUser.getAddress() == null || purchasingUser.getAddress().isBlank())) {
            throw new IllegalArgumentException("Add an address to your account before checkout.");
        }
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }
        String normalizedMethod = paymentMethod.toLowerCase(Locale.ROOT);
        if (!normalizedMethod.equals("qr") && !normalizedMethod.equals("card")) {
            throw new IllegalArgumentException("Choose QR code or credit card.");
        }
        Long userId = Objects.requireNonNull(purchasingUser.getId());
        var latestOrder = orders.findTopByUser_IdOrderByOrderNumberDesc(userId);
        int orderNumber = latestOrder.isPresent()
                ? Objects.requireNonNull(latestOrder.get()).getOrderNumber() + 1
                : 1;
        CustomerOrder order = new CustomerOrder(purchasingUser, normalizedMethod, orderNumber);
        cart.getItems().forEach(item -> {
            Product product = products.findByIdForUpdate(
                            Objects.requireNonNull(item.product().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));
            try {
                product.decreaseStock(item.quantity());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(
                        product.getName() + " no longer has enough stock.", exception);
            }
            order.addItem(product, item.quantity());
        });
        CustomerOrder saved = orders.save(order);
        cart.clear();
        return saved;
    }
}
