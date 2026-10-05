package com.thg.chickenshop.repository;

import com.thg.chickenshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> { }
