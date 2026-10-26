package com.thg.chickenshop.controller;

import com.thg.chickenshop.model.Product;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.service.CartService;
import com.thg.chickenshop.service.CategoryService;
import com.thg.chickenshop.service.CurrentUserService;
import com.thg.chickenshop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private static final int PAGE_SIZE = 8;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             CartService cartService,
                             CurrentUserService currentUserService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/menu")
    public String menu(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Long category,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {

        Page<Product> products = productService.search(q, category, page, PAGE_SIZE);
        User user = currentUserService.getCurrentUser();

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("cartCount", cartService.countItems(user));
        model.addAttribute("cartTotal", cartService.getTotal(user));
        model.addAttribute("active", "menu");
        return "menu";
    }
}