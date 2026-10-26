package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.CategoryForm;
import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.exception.NotFoundException;
import com.thg.chickenshop.model.Category;
import com.thg.chickenshop.repository.CategoryRepository;
import com.thg.chickenshop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;      // MỚI

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository) {   // MỚI
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục"));
    }

    public CategoryForm getForm(Long id) {                   // MỚI
        Category c = findById(id);
        CategoryForm form = new CategoryForm();
        form.setId(c.getId());
        form.setName(c.getName());
        return form;
    }

    @Transactional
    public Category save(CategoryForm form) {                // MỚI
        String name = form.getName().trim();

        boolean duplicate = (form.getId() == null)
                ? categoryRepository.existsByNameIgnoreCase(name)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(name, form.getId());
        if (duplicate) {
            throw new BusinessException("Tên danh mục đã tồn tại");
        }

        Category c = (form.getId() == null) ? new Category() : findById(form.getId());
        c.setName(name);
        return categoryRepository.save(c);
    }

    @Transactional
    public void delete(Long id) {                            // MỚI
        Category c = findById(id);
        if (productRepository.countByCategoryId(id) > 0) {
            throw new BusinessException(
                    "Không thể xóa: danh mục vẫn còn món ăn. Hãy chuyển các món sang danh mục khác trước.");
        }
        categoryRepository.delete(c);
    }
}