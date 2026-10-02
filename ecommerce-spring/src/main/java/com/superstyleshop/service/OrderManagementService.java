package com.superstyleshop.service;

import com.superstyleshop.model.CustomerOrder;
import com.superstyleshop.model.UserAccount;
import com.superstyleshop.repository.CustomerOrderRepository;
import com.superstyleshop.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderManagementService {
    private static final String ACCOUNT_NOT_FOUND = "Account not found.";

    private final CustomerOrderRepository orders;
    private final UserAccountRepository users;

    public OrderManagementService(CustomerOrderRepository orders, UserAccountRepository users) {
        this.orders = orders;
        this.users = users;
    }

    public List<CustomerOrder> getOrdersForUser(Long userId) {
        return orders.findByUser_IdOrderByOrderNumberAsc(Objects.requireNonNull(userId));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPurchases(Long userId) {
        BigDecimal total = BigDecimal.ZERO;
        for (CustomerOrder order : orders.findByUser_IdOrderByOrderNumberAsc(
                Objects.requireNonNull(userId))) {
            total = total.add(Objects.requireNonNull(order).getTotalAmount());
        }
        return total;
    }

    @Transactional
    public void deleteOwnOrder(Long userId, Long orderId) {
        users.findByIdForUpdate(Objects.requireNonNull(userId))
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));
        CustomerOrder order = getOrder(orderId);
        if (order.getUser() == null || !Objects.equals(order.getUser().getId(), userId)) {
            throw new IllegalArgumentException("That order does not belong to your account.");
        }
        deleteOrderAndRenumber(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        CustomerOrder order = getOrder(orderId);
        Long userId = order.getUser() == null ? null : order.getUser().getId();
        if (userId != null) {
            users.findByIdForUpdate(userId)
                    .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));
        }
        deleteOrderAndRenumber(order);
    }

    @Transactional
    public void updateOrderItem(Long orderId, Long itemId, int quantity, BigDecimal unitPrice) {
        CustomerOrder order = getOrder(orderId);
        order.updateItem(itemId, quantity, unitPrice);
        orders.save(order);
    }

    @Transactional
    public void deleteUser(Long userId) {
        UserAccount user = users.findByIdForUpdate(Objects.requireNonNull(userId))
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));
        if (user.isAdmin()) {
            throw new IllegalArgumentException("Administrator accounts cannot be deleted.");
        }
        List<CustomerOrder> userOrders = orders.findByUser_IdOrderByOrderNumberAsc(userId);
        orders.deleteAll(Objects.requireNonNull(userOrders));
        users.delete(user);
    }

    private void renumberUserOrders(Long userId) {
        List<CustomerOrder> userOrders = orders.findByUser_IdOrderByCreatedAtAscIdAsc(userId);
        for (int index = 0; index < userOrders.size(); index++) {
            userOrders.get(index).setOrderNumber(index + 1);
        }
        orders.saveAll(userOrders);
    }

    private CustomerOrder getOrder(Long orderId) {
        return orders.findById(Objects.requireNonNull(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
    }

    private void deleteOrderAndRenumber(CustomerOrder order) {
        Long userId = order.getUser() == null ? null : order.getUser().getId();
        orders.delete(order);
        if (userId != null) {
            renumberUserOrders(userId);
        }
    }
}
