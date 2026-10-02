package com.superstyleshop.web;

import com.superstyleshop.model.CustomerOrder;
import com.superstyleshop.model.Product;
import com.superstyleshop.repository.CustomerOrderRepository;
import com.superstyleshop.repository.ProductRepository;
import com.superstyleshop.repository.UserAccountRepository;
import com.superstyleshop.service.AdminManagementService;
import com.superstyleshop.service.OrderManagementService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Validated
public class AdminController {
    private static final String NOTICE = "notice";
    private static final String ERROR = "error";
    private static final String ADMIN_REDIRECT = "redirect:/admin";

    private final ProductRepository products;
    private final UserAccountRepository users;
    private final CustomerOrderRepository orders;
    private final AdminManagementService adminManagement;
    private final OrderManagementService orderManagement;

    public AdminController(
            ProductRepository products,
            UserAccountRepository users,
            CustomerOrderRepository orders,
            AdminManagementService adminManagement,
            OrderManagementService orderManagement) {
        this.products = products;
        this.users = users;
        this.orders = orders;
        this.adminManagement = adminManagement;
        this.orderManagement = orderManagement;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        List<CustomerOrder> orderList = orders.findAllByOrderByCreatedAtDesc();
        List<Product> productList = products.findAll(Sort.by("name").ascending());
        model.addAttribute("products", productList);
        model.addAttribute("users", users.findAllByOrderByEmailAsc());
        model.addAttribute("orders", orderList);
        model.addAttribute("cartCount", 0);
        return "admin";
    }

    @PostMapping("/admin/products/{productId}")
    public String updateProduct(
            @PathVariable Long productId,
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal price,
            @RequestParam @Min(0) int stockQuantity,
            RedirectAttributes redirectAttributes) {
        try {
            adminManagement.updateProduct(productId, price, stockQuantity);
            redirectAttributes.addFlashAttribute(NOTICE, "Product price and stock were updated.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(ERROR, Objects.requireNonNull(exception.getMessage()));
        }
        return ADMIN_REDIRECT;
    }

    @PostMapping("/admin/users/{userId}/delete")
    public String deleteUser(
            @PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            orderManagement.deleteUser(userId);
            redirectAttributes.addFlashAttribute(NOTICE, "User and their order history were deleted.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(ERROR, Objects.requireNonNull(exception.getMessage()));
        }
        return ADMIN_REDIRECT;
    }

    @PostMapping("/admin/orders/{orderId}/items/{itemId}")
    public String updateOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestParam @Min(1) @Max(99) int quantity,
            @RequestParam @NotNull @DecimalMin("0.01") BigDecimal unitPrice,
            RedirectAttributes redirectAttributes) {
        try {
            orderManagement.updateOrderItem(orderId, itemId, quantity, unitPrice);
            redirectAttributes.addFlashAttribute(NOTICE, "Order item was updated.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(ERROR, Objects.requireNonNull(exception.getMessage()));
        }
        return ADMIN_REDIRECT;
    }

    @PostMapping("/admin/orders/{orderId}/delete")
    public String deleteOrder(
            @PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            orderManagement.deleteOrder(orderId);
            redirectAttributes.addFlashAttribute(NOTICE, "Order history entry was deleted.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(ERROR, Objects.requireNonNull(exception.getMessage()));
        }
        return ADMIN_REDIRECT;
    }
}
