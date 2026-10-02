package com.superstyleshop.web;

import com.superstyleshop.model.Product;
import com.superstyleshop.repository.ProductRepository;
import com.superstyleshop.service.CartService;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class StoreController {
    private final ProductRepository products;
    private final CartService cart;

    public StoreController(ProductRepository products, CartService cart) {
        this.products = products;
        this.cart = cart;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "All") String category,
            Model model) {
        String search = q.trim();
        List<Product> matches;
        if (!category.equalsIgnoreCase("All") && !search.isBlank()) {
            matches = products.findByCategoryIgnoreCaseAndNameContainingIgnoreCaseOrderByNameAsc(
                    category, search);
        } else if (!category.equalsIgnoreCase("All")) {
            matches = products.findByCategoryIgnoreCaseOrderByNameAsc(category);
        } else if (!search.isBlank()) {
            matches = products.findByNameContainingIgnoreCaseOrderByNameAsc(search);
        } else {
            matches = products.findAll(Sort.by("name").ascending());
        }
        model.addAttribute("products", matches);
        model.addAttribute("categories", List.of("All", "Electronics", "Clothing", "Books"));
        model.addAttribute("selectedCategory", category);
        model.addAttribute("search", search);
        addCartState(model);
        return "home";
    }

    @GetMapping("/products/{id}")
    public String product(@PathVariable Long id, Model model) {
        Product product = products.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("product", product);
        addCartState(model);
        return "product";
    }

    private void addCartState(Model model) {
        model.addAttribute("cartCount", cart.getItemCount());
    }
}
