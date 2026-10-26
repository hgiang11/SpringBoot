package com.thg.chickenshop.repository;

import com.thg.chickenshop.model.Cart;
import com.thg.chickenshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}