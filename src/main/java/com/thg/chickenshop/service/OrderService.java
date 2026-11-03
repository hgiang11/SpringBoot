package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.CheckoutForm;
import com.thg.chickenshop.dto.DeliveryQuote;
import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.exception.NotFoundException;
import com.thg.chickenshop.model.*;
import com.thg.chickenshop.repository.CartRepository;
import com.thg.chickenshop.repository.OrderRepository;
import org.springframework.context.i18n.LocaleContextHolder;
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

    // [MỚI] Giữ nguyên chữ ký cũ để test và code cũ vẫn gọi được
    @Transactional
    public Order placeOrder(User user, CheckoutForm form) {
        return placeOrder(user, form, null);
    }

    @Transactional
    public Order placeOrder(User user, CheckoutForm form, DeliveryQuote quote) {   // [ĐỔI] thêm tham số quote
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("checkout.emptyCart"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("checkout.emptyCart");
        }

        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress(form.getDeliveryAddress().trim());
        order.setPhone(form.getPhone().trim());
        // [MỚI] Tên người nhận; nếu form không gửi thì lấy tên tài khoản (giữ tương thích test cũ)
        String receiver = form.getReceiverName();
        order.setReceiverName(receiver != null && !receiver.isBlank() ? receiver.trim() : user.getFullName());
        // [MỚI] Hình thức thanh toán, mặc định COD
        order.setPaymentMethod(form.getPaymentMethod() != null ? form.getPaymentMethod() : PaymentMethod.COD);
        order.setStatus(OrderStatus.PENDING);

        // [MỚI] Kết quả tính phí do server tính, không lấy từ trình duyệt
        if (quote != null) {
            order.setDistanceKm(quote.distanceKm());
            order.setShippingFee(quote.shippingFee());
            order.setEstimatedMinutes(quote.totalMinutes());
            order.setEstimatedDeliveryTime(quote.eta());
            order.setDeliveryLat(form.getDeliveryLat());
            order.setDeliveryLng(form.getDeliveryLng());
        }


        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            if (!p.isAvailable()) {
                String name = p.localizedName(LocaleContextHolder.getLocale().getLanguage());
                throw new BusinessException("order.productUnavailable", name);
            }
            // [MỚI] kiểm tra và trừ kho
            if (p.getStock() < ci.getQuantity()) {
                throw new BusinessException("order.outOfStock",
                        p.localizedName(LocaleContextHolder.getLocale().getLanguage()));
            }
            p.decreaseStock(ci.getQuantity());

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
            throw new BusinessException("order.cancelOnlyPending");
        }
        order.setStatus(OrderStatus.CANCELLED);
        restoreStock(order);   // [MỚI]
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

    // [MỚI] Trả hàng về kho khi đơn bị hủy
    private void restoreStock(Order order) {
        for (OrderItem oi : order.getItems()) {
            Product p = oi.getProduct();
            p.setStock(p.getStock() + oi.getQuantity());
        }
    }
}