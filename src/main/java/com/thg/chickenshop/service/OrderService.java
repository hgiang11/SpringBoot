package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.CheckoutForm;
import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.exception.NotFoundException;
import com.thg.chickenshop.model.*;
import com.thg.chickenshop.repository.CartRepository;
import com.thg.chickenshop.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    // ===== Dành cho khách =====

    @Transactional
    public Order placeOrder(User user, CheckoutForm form) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("checkout.emptyCart"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("checkout.emptyCart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress(form.getDeliveryAddress().trim());
        order.setPhone(form.getPhone().trim());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            if (!p.isAvailable()) {
                throw new BusinessException("Món \"" + p.getName()
                        + "\" hiện không còn bán, vui lòng xóa khỏi giỏ hàng");
            }
            // chốt giá tại thời điểm đặt: đổi giá món sau này không ảnh hưởng đơn cũ
            OrderItem oi = new OrderItem(p, ci.getQuantity(), p.getPrice());
            order.addItem(oi);
            total = total.add(oi.getSubtotal());
        }
        order.setTotalPrice(total);

        cart.getItems().clear();                 // đặt xong thì làm trống giỏ
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> findByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    // Chỉ trả đơn của CHÍNH người dùng này, đơn của người khác coi như không tồn tại
    @Transactional(readOnly = true)
    public Order findByIdForUser(Long id, User user) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.notFound"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("order.notFound");
        }
        return order;
    }

    @Transactional
    public void cancel(Long id, User user) {
        Order order = findByIdForUser(id, user);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("order.cancelOnlyPending"); }
        order.setStatus(OrderStatus.CANCELLED);
    }

    // ===== Dành cho admin =====

    @Transactional(readOnly = true)
    public Page<Order> findAllForAdmin(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return (status == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order.notFound"));
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatus next) {
        Order order = findById(id);
        if (!order.getStatus().canChangeTo(next)) {
            throw new BusinessException("Không thể chuyển từ \"" + order.getStatus().getLabel()
                    + "\" sang \"" + next.getLabel() + "\"");
        }
        order.setStatus(next);
        return order;
    }
}