package com.thg.chickenshop.controller;

import com.thg.chickenshop.dto.ProductForm;
import com.thg.chickenshop.service.CategoryService;
import com.thg.chickenshop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService,
                                  CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("products", productService.findAllForAdmin(q, page, 10));
        model.addAttribute("q", q);
        return "admin/products";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("formTitle", "Thêm món mới");
        return "admin/product-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("productForm", productService.getForm(id));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("formTitle", "Sửa món ăn");
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("productForm") ProductForm form,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {   // có lỗi: trả lại form, KHÔNG redirect
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formTitle",
                    form.getId() == null ? "Thêm món mới" : "Sửa món ăn");
            return "admin/product-form";
        }

        productService.save(form);
        redirectAttributes.addFlashAttribute("message", "Đã lưu món ăn thành công");
        return "redirect:/admin/products";
    }
}