package com.thg.chickenshop.controller;

import com.thg.chickenshop.dto.CategoryForm;
import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        model.addAttribute("formTitle", "Thêm danh mục");
        return "admin/category-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("categoryForm", categoryService.getForm(id));
        model.addAttribute("formTitle", "Sửa danh mục");
        return "admin/category-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        if (!result.hasErrors()) {
            try {
                categoryService.save(form);
                redirectAttributes.addFlashAttribute("message", "Đã lưu danh mục thành công");
                return "redirect:/admin/categories";
            } catch (BusinessException ex) {
                result.rejectValue("name", "category.duplicate", ex.getMessage());
            }
        }

        model.addAttribute("formTitle",
                form.getId() == null ? "Thêm danh mục" : "Sửa danh mục");
        return "admin/category-form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("message", "Đã xóa danh mục");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}