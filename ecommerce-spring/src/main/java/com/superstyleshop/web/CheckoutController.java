package com.superstyleshop.web;

import com.superstyleshop.model.CustomerOrder;
import com.superstyleshop.model.UserAccount;
import com.superstyleshop.repository.CustomerOrderRepository;
import com.superstyleshop.repository.UserAccountRepository;
import com.superstyleshop.service.CartService;
import com.superstyleshop.service.CheckoutService;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckoutController {
    private static final String ERROR = "error";

    private final CartService cart;
    private final CheckoutService checkout;
    private final UserAccountRepository users;
    private final CustomerOrderRepository orders;

    public CheckoutController(
            CartService cart,
            CheckoutService checkout,
            UserAccountRepository users,
            CustomerOrderRepository orders) {
        this.cart = cart;
        this.checkout = checkout;
        this.users = users;
        this.orders = orders;
    }

    @GetMapping("/checkout")
    public String checkout(Authentication authentication, Model model) {
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        UserAccount user = getCurrentAccount(authentication);
        model.addAttribute("items", cart.getItems());
        model.addAttribute("total", cart.getTotal());
        model.addAttribute("cartCount", cart.getItemCount());
        model.addAttribute("needsLogin", user == null);
        model.addAttribute("addressMissing", user != null && !user.isAdmin()
                && (user.getAddress() == null || user.getAddress().isBlank()));
        model.addAttribute("canPay", user != null && (user.isAdmin()
                || (user.getAddress() != null && !user.getAddress().isBlank())));
        return "checkout";
    }

    @PostMapping("/checkout/buy-now")
    public String buyNow(@RequestParam Long productId, RedirectAttributes redirectAttributes) {
        try {
            cart.buyNow(productId);
            return "redirect:/checkout";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(ERROR, exception.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/checkout")
    public String pay(
            @RequestParam String paymentMethod,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        UserAccount user = getCurrentAccount(authentication);
        if (user == null) {
            redirectAttributes.addFlashAttribute("notice", "Log in to your account before checkout.");
            return "redirect:/login";
        }
        if (!user.isAdmin() && (user.getAddress() == null || user.getAddress().isBlank())) {
            redirectAttributes.addFlashAttribute(ERROR, "Add an address to your account before checkout.");
            return "redirect:/account";
        }
        try {
            CustomerOrder order = checkout.placeOrder(cart, user, paymentMethod);
            return "redirect:/checkout/success/" + order.getId();
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(ERROR, exception.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/checkout/success/{id}")
    public String success(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            Authentication authentication,
            Model model) {
        CustomerOrder order = orders.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserAccount currentUser = getCurrentAccount(authentication);
        if (id < 1 || currentUser == null || order.getUser() == null
                || !Objects.equals(order.getUser().getId(), currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("orderId", order.getOrderNumber());
        model.addAttribute("cartCount", cart.getItemCount());
        return "order-success";
    }

    private UserAccount getCurrentAccount(Authentication authentication) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof UserAccount principal)) {
            return null;
        }
        return users.findById(Objects.requireNonNull(principal.getId()))
                .orElseThrow(() -> new IllegalStateException("Signed-in account no longer exists."));
    }
}
