package com.superstyleshop.web;

import com.superstyleshop.model.UserAccount;
import com.superstyleshop.repository.ProductRepository;
import com.superstyleshop.repository.UserAccountRepository;
import com.superstyleshop.service.AccountService;
import com.superstyleshop.service.OrderManagementService;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {
    private final AccountService accounts;
    private final UserAccountRepository users;
    private final OrderManagementService orderManagement;
    private final ProductRepository products;

    static final String SIGNUP = "signup";
    static final String CARTCOUNT = "cartCount";
    static final String NOTICE = "notice";
    private static final String ERROR = "error";
    private static final String ACCOUNT_REDIRECT = "redirect:/account";

    public AccountController(
            AccountService accounts,
            UserAccountRepository users,
            OrderManagementService orderManagement,
            ProductRepository products) {
        this.accounts = accounts;
        this.users = users;
        this.orderManagement = orderManagement;
        this.products = products;
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("form", new SignupForm());
        return SIGNUP;
    }

    @PostMapping("/signup")
    public String register(
            @ModelAttribute("form") @jakarta.validation.Valid SignupForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match.");
        }
        if (users.existsByEmailIgnoreCase(form.getEmail())) {
            binding.rejectValue("email", "email.exists", "An account with that email already exists.");
        }
        if (binding.hasErrors()) {
            model.addAttribute(CARTCOUNT, 0);
            return SIGNUP;
        }
        try {
            accounts.register(form.getEmail(), form.getPassword(), form.getFullName());
        } catch (IllegalArgumentException exception) {
            binding.rejectValue("email", "email.exists", Objects.requireNonNull(exception.getMessage()));
            return SIGNUP;
        }
        redirectAttributes.addFlashAttribute(NOTICE, "Account created. Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute(CARTCOUNT, 0);
        return "login";
    }

    @GetMapping("/account")
    public String account(
            @AuthenticationPrincipal UserAccount principal,
            @RequestParam(required = false) Integer orderNumber,
            @RequestParam(defaultValue = "") String itemName,
            @RequestParam(defaultValue = "") String category,
            Model model) {
        UserAccount user = users.findById(Objects.requireNonNull(principal.getId()))
                .orElseThrow(() -> new IllegalStateException("Signed-in account no longer exists."));
        String normalizedItemName = itemName.trim();
        List<OrderHistoryView> history = orderManagement.getOrdersForUser(user.getId()).stream()
                .filter(order -> orderNumber == null || order.getOrderNumber() == orderNumber)
                .map(order -> new OrderHistoryView(order, order.getItems().stream()
                        .filter(line -> normalizedItemName.isBlank()
                                || line.getProduct().getName().toLowerCase()
                                        .contains(normalizedItemName.toLowerCase(Locale.ROOT)))
                        .filter(line -> category.isBlank()
                                || line.getProduct().getCategory().equalsIgnoreCase(category))
                        .toList()))
                .filter(entry -> !entry.items().isEmpty())
                .toList();
        model.addAttribute("user", user);
        model.addAttribute(CARTCOUNT, 0);
        model.addAttribute("orders", history);
        model.addAttribute("totalPurchases", orderManagement.getTotalPurchases(user.getId()));
        model.addAttribute("orderNumberFilter", orderNumber);
        model.addAttribute("itemNameFilter", normalizedItemName);
        model.addAttribute("categoryFilter", category);
        List<String> categoryOptions = products.findAll().stream()
                .map(product -> Objects.requireNonNull(product).getCategory())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
        model.addAttribute("categories", categoryOptions);
        return "account";
    }

    @PostMapping("/account/orders/{orderId}/delete")
    public String deleteOwnOrder(
            @AuthenticationPrincipal UserAccount principal,
            @org.springframework.web.bind.annotation.PathVariable Long orderId,
            RedirectAttributes redirectAttributes) {
        try {
            orderManagement.deleteOwnOrder(
                    Objects.requireNonNull(principal.getId()), orderId);
            redirectAttributes.addFlashAttribute(NOTICE, "Order history entry deleted.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(ERROR, exception.getMessage());
        }
        return ACCOUNT_REDIRECT;
    }

    @PostMapping("/account")
    @Validated
    public String updateAccount(
            @AuthenticationPrincipal UserAccount principal,
            @RequestParam @NotBlank @Size(max = 120) String fullName,
            @RequestParam(required = false) @Size(max = 1000) String address,
            RedirectAttributes redirectAttributes) {
        UserAccount user = users.findById(Objects.requireNonNull(principal.getId()))
                .orElseThrow(() -> new IllegalStateException("Signed-in account no longer exists."));
        try {
            accounts.updateProfile(user, fullName, address);
            redirectAttributes.addFlashAttribute(NOTICE, "Your profile has been updated.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(ERROR, exception.getMessage());
        }
        return ACCOUNT_REDIRECT;
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute(CARTCOUNT, 0);
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(
            @RequestParam String email, RedirectAttributes redirectAttributes) {
        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute(ERROR, "Enter an email address.");
            return "redirect:/forgot-password";
        }
        redirectAttributes.addFlashAttribute(
                NOTICE, "If an account exists for that email, reset instructions would be sent.");
        return "redirect:/forgot-password";
    }
}

class SignupForm {
    @NotBlank
    @jakarta.validation.constraints.Email
    @Size(max = 254)
    private String email = "";

    @NotBlank
    @Size(min = 8, max = 72)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one number.")
    private String password = "";

    @NotBlank
    private String confirmPassword = "";

    @NotBlank
    @Size(max = 120)
    private String fullName = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
