package com.superstyleshop;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.superstyleshop.model.UserAccount;
import com.superstyleshop.model.CustomerOrder;
import com.superstyleshop.repository.CustomerOrderRepository;
import com.superstyleshop.repository.ProductRepository;
import com.superstyleshop.repository.UserAccountRepository;
import com.superstyleshop.service.AccountService;
import com.superstyleshop.service.AdminManagementService;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:superstyleshop-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "app.admin.email=admin-test@example.com",
        "app.admin.password=test-admin-password-123",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
class StoreApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accounts;

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private CustomerOrderRepository orders;

    @Autowired
    private ProductRepository products;

    @Autowired
    private AdminManagementService adminManagement;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void homeShowsSeededCatalogAndSearchFiltersProducts() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Wireless Headphones"))))
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Bestseller Novel"))));

        mockMvc.perform(get("/").param("q", "headphones"))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Wireless Headphones"))))
                .andExpect(content().string(Objects.requireNonNull(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Bestseller Novel")))));
    }

    @Test
    void registrationValidatesAndCreatesAccount() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Create an account"))));

        mockMvc.perform(post("/signup")
                        .param("email", "shopper@example.com")
                        .param("password", "demo-password1")
                        .param("confirmPassword", "demo-password1")
                        .param("fullName", "Demo Shopper")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        Assertions.assertTrue(users.existsByEmailIgnoreCase("shopper@example.com"));
    }

    @Test
    void registrationRequiresLettersAndDigitsInPassword() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("email", "letters-only@example.com")
                        .param("password", "letters-only")
                        .param("confirmPassword", "letters-only")
                        .param("fullName", "Letters Only")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString(
                                "Password must contain at least one letter and one number."))));
        Assertions.assertFalse(users.existsByEmailIgnoreCase("letters-only@example.com"));

        mockMvc.perform(post("/signup")
                        .param("email", "digits-only@example.com")
                        .param("password", "12345678")
                        .param("confirmPassword", "12345678")
                        .param("fullName", "Digits Only")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString(
                                "Password must contain at least one letter and one number."))));
        Assertions.assertFalse(users.existsByEmailIgnoreCase("digits-only@example.com"));
    }

    @Test
    void adminAccountIsSeededWithEncodedPassword() {
        UserAccount admin = users.findByEmailIgnoreCase("admin-test@example.com").orElseThrow();

        Assertions.assertTrue(admin.isAdmin());
        Assertions.assertTrue(passwordEncoder.matches("test-admin-password-123", admin.getPassword()));
    }

    @Test
    void adminDashboardUpdatesPriceAndStockAndProtectsAdminAccount() throws Exception {
        UserAccount admin = users.findByEmailIgnoreCase("admin-test@example.com").orElseThrow();
        var originalProduct = products.findById(1L).orElseThrow();
        int originalStock = originalProduct.getStockQuantity();
        var originalPrice = originalProduct.getPrice();

        mockMvc.perform(get("/admin")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(admin))))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Admin dashboard"))));

        mockMvc.perform(post("/admin/products/1")
                        .param("price", "123.45")
                        .param("stockQuantity", "5")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(admin)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        Assertions.assertEquals(0, new java.math.BigDecimal("123.45")
                .compareTo(products.findById(1L).orElseThrow().getPrice()));
        Assertions.assertEquals(5, products.findById(1L).orElseThrow().getStockQuantity());

        mockMvc.perform(post("/admin/users/{id}/delete", admin.getId())
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(admin)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection());
        Assertions.assertTrue(users.existsByEmailIgnoreCase("admin-test@example.com"));

        adminManagement.updateProduct(1L, originalPrice, originalStock);
    }

    @Test
    void cartCanBeUpdatedAndDemoCheckoutRecordsOrder() throws Exception {
        int stockBeforePurchase = products.findById(1L).orElseThrow().getStockQuantity();
        var session = mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getRequest()
                .getSession();

        mockMvc.perform(get("/checkout").session(Objects.requireNonNull(
                        (org.springframework.mock.web.MockHttpSession) session)))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Log in to your account"))))
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Place demo order")))));

        mockMvc.perform(post("/checkout")
                        .param("paymentMethod", "qr")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/cart").session(Objects.requireNonNull(
                (org.springframework.mock.web.MockHttpSession) session)))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Wireless Headphones"))));

        UserAccount buyer = accounts.register("checkout@example.com", "test-password1", "Checkout Buyer");
        accounts.updateProfile(buyer, "Checkout Buyer", "123 Sample Street");
        mockMvc.perform(post("/checkout")
                        .param("paymentMethod", "qr")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(buyer)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrlPattern("/checkout/success/*"));
        Assertions.assertEquals(
                stockBeforePurchase - 1, products.findById(1L).orElseThrow().getStockQuantity());
    }

    @Test
    void checkoutRequiresAddressForNonAdminAccounts() throws Exception {
        var session = mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andReturn()
                .getRequest()
                .getSession();
        long existingOrders = orders.count();
        UserAccount buyer = accounts.register(
                "address-required@example.com", "test-password1", "Address Required");

        mockMvc.perform(post("/checkout")
                        .param("paymentMethod", "qr")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(buyer)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"));

        Assertions.assertEquals(existingOrders, orders.count());

        accounts.updateProfile(buyer, "Address Required", "456 Updated Street");
        mockMvc.perform(post("/checkout")
                        .param("paymentMethod", "qr")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(buyer)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrlPattern("/checkout/success/*"));
        Assertions.assertEquals(existingOrders + 1, orders.count());
    }

    @Test
    void seededAdminCanPayWithoutAddress() throws Exception {
        var session = mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andReturn()
                .getRequest()
                .getSession();
        UserAccount admin = users.findByEmailIgnoreCase("admin-test@example.com").orElseThrow();

        mockMvc.perform(post("/checkout")
                        .param("paymentMethod", "qr")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(admin)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrlPattern("/checkout/success/*"));
    }

    @Test
    void accountOrderNumbersAreIndependentAndDeletingHistoryRenumbersOrders() throws Exception {
        UserAccount firstBuyer = accounts.register(
                "numbering-one@example.com", "test-password1", "Numbering One");
        accounts.updateProfile(firstBuyer, "Numbering One", "1 Numbering Street");
        UserAccount secondBuyer = accounts.register(
                "numbering-two@example.com", "test-password1", "Numbering Two");
        accounts.updateProfile(secondBuyer, "Numbering Two", "2 Numbering Street");

        var session = mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andReturn()
                .getRequest()
                .getSession();
        mockMvc.perform(post("/checkout")
                        .param("paymentMethod", "qr")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(firstBuyer)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection());
        Long firstOrderId = orders.findByUser_IdOrderByOrderNumberAsc(firstBuyer.getId()).get(0).getId();

        mockMvc.perform(post("/cart/add")
                        .param("productId", "2")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())));
        mockMvc.perform(post("/checkout")
                        .param("paymentMethod", "qr")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(firstBuyer)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection());

        CustomerOrder secondOrder = orders.findByUser_IdOrderByOrderNumberAsc(firstBuyer.getId()).get(1);
        var secondOrderLine = secondOrder.getItems().get(0);
        UserAccount admin = users.findByEmailIgnoreCase("admin-test@example.com").orElseThrow();
        mockMvc.perform(post("/admin/orders/{orderId}/items/{itemId}",
                        secondOrder.getId(), secondOrderLine.getId())
                        .param("quantity", "2")
                        .param("unitPrice", "5.00")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(admin)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection());
        Assertions.assertEquals(0, new java.math.BigDecimal("10.00")
                .compareTo(orders.findById(Objects.requireNonNull(secondOrder.getId()))
                        .orElseThrow().getTotalAmount()));

        mockMvc.perform(post("/cart/add")
                        .param("productId", "3")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())));
        mockMvc.perform(post("/checkout")
                        .param("paymentMethod", "qr")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(secondBuyer)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection());

        Assertions.assertEquals(List.of(1, 2), orders.findByUser_IdOrderByOrderNumberAsc(firstBuyer.getId())
                .stream().map(order -> Objects.requireNonNull(order).getOrderNumber()).toList());
        Assertions.assertEquals(1, orders.findByUser_IdOrderByOrderNumberAsc(secondBuyer.getId())
                .get(0).getOrderNumber());
        mockMvc.perform(get("/account")
                        .param("orderNumber", "2")
                        .param("itemName", "watch")
                        .param("category", "Electronics")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(firstBuyer))))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Order #2"))))
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Smart Watch"))));

        mockMvc.perform(post("/account/orders/{id}/delete", firstOrderId)
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(firstBuyer)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection());

        Assertions.assertEquals(1, orders.findByUser_IdOrderByOrderNumberAsc(firstBuyer.getId())
                .get(0).getOrderNumber());
        mockMvc.perform(get("/account")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(firstBuyer))))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Order history"))));

        mockMvc.perform(post("/admin/users/{id}/delete", secondBuyer.getId())
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.user(admin)))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection());
        Assertions.assertFalse(users.existsByEmailIgnoreCase("numbering-two@example.com"));
        Assertions.assertTrue(orders.findByUser_IdOrderByOrderNumberAsc(secondBuyer.getId()).isEmpty());
    }

    @Test
    void buyNowReplacesExistingCartWithSelectedProduct() throws Exception {
        var session = mockMvc.perform(post("/cart/add")
                        .param("productId", "1")
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andReturn()
                .getRequest()
                .getSession();

        mockMvc.perform(post("/checkout/buy-now")
                        .param("productId", "2")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session))
                        .with(Objects.requireNonNull(org.springframework.security.test.web.servlet
                                .request.SecurityMockMvcRequestPostProcessors.csrf())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/checkout"));

        mockMvc.perform(get("/checkout")
                        .session(Objects.requireNonNull(
                                (org.springframework.mock.web.MockHttpSession) session)))
                .andExpect(status().isOk())
                .andExpect(content().string(Objects.requireNonNull(
                        org.hamcrest.Matchers.containsString("Smart Watch"))))
                .andExpect(content().string(Objects.requireNonNull(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Wireless Headphones")))));
    }
}
