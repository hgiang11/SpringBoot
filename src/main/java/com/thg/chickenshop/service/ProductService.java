package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.ProductForm;
import com.thg.chickenshop.exception.NotFoundException;
import com.thg.chickenshop.model.Product;
import com.thg.chickenshop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;     // MỚI

    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService) {   // MỚI: thêm tham số
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    // ===== Dành cho khách (Phần A) - GIỮ NGUYÊN =====
    public Page<Product> search(String keyword, Long categoryId, int page, int size) {
        String kw = (keyword == null) ? "" : keyword.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return productRepository.search(categoryId, kw, pageable);
    }

    // ===== Dành cho admin (Phần B1) - MỚI =====
    public Page<Product> findAllForAdmin(String keyword, int page, int size) {
        String kw = (keyword == null) ? "" : keyword.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productRepository.findByNameContainingIgnoreCase(kw, pageable);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy món ăn"));
    }

    @Transactional(readOnly = true)
    public ProductForm getForm(Long id) {
        Product p = findById(id);
        ProductForm form = new ProductForm();
        form.setId(p.getId());
        form.setName(p.getName());
        form.setDescription(p.getDescription());
        form.setPrice(p.getPrice());
        form.setImageUrl(p.getImageUrl());
        form.setCategoryId(p.getCategory().getId());
        form.setAvailable(p.isAvailable());
        return form;
    }

    @Transactional
    public Product save(ProductForm form) {
        Product p = (form.getId() == null) ? new Product() : findById(form.getId());
        p.setName(form.getName().trim());
        p.setDescription(form.getDescription());
        p.setPrice(form.getPrice());
        p.setImageUrl(form.getImageUrl());
        p.setAvailable(form.isAvailable());
        p.setCategory(categoryService.findById(form.getCategoryId()));
        return productRepository.save(p);
    }
}