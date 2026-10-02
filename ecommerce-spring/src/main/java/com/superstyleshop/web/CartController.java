package com.superstyleshop.web;

import com.superstyleshop.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {
    private final CartService cart;

    public CartController(CartService cart) {
        this.cart = cart;
    }

    @PostMapping("/cart/add")
    public String add(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes) {
        try {
            cart.add(productId, quantity);
            redirectAttributes.addFlashAttribute("notice", "Added to your cart.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/cart/update")
    public String update(
            @RequestParam Long productId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {
        try {
            cart.update(productId, quantity);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String remove(@RequestParam Long productId) {
        cart.remove(productId);
        return "redirect:/cart";
    }

    @org.springframework.web.bind.annotation.GetMapping("/cart")
    public String view(Model model) {
        model.addAttribute("items", cart.getItems());
        model.addAttribute("total", cart.getTotal());
        model.addAttribute("cartCount", cart.getItemCount());
        return "cart";
    }
}
