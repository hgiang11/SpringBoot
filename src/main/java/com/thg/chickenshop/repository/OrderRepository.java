package com.thg.chickenshop.repository;

import com.thg.chickenshop.model.Order;
import com.thg.chickenshop.model.OrderStatus;
import com.thg.chickenshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}