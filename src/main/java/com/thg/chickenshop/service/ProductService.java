package com.thg.chickenshop.service;

import com.thg.chickenshop.model.Product;
import com.thg.chickenshop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> search(String keyword, Long categoryId, int page, int size) {
        String kw = (keyword == null) ? "" : keyword.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return productRepository.search(categoryId, kw, pageable);
    }
}
