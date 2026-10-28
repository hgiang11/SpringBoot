package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.CheckoutForm;
import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.exception.NotFoundException;
import com.thg.chickenshop.model.*;
import com.thg.chickenshop.repository.CartRepository;
import com.thg.chickenshop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    // ===== Hàm tiện ích tạo dữ liệu mẫu =====

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        return u;
    }

    private Product product(Long id, String name, String price, boolean available) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setAvailable(available);
        return p;
    }

    private CheckoutForm checkoutForm() {
        CheckoutForm form = new CheckoutForm();
        form.setDeliveryAddress("123 Duong ABC");
        form.setPhone("0912345678");
        return form;
    }

    private Order orderOf(Long id, User owner, OrderStatus status) {
        Order o = new Order();
        o.setId(id);
        o.setUser(owner);
        o.setStatus(status);
        return o;
    }

    // ===== placeOrder =====

    @Test
    @DisplayName("Đặt hàng thành công: tạo đơn, tính đúng tổng tiền, làm trống giỏ")
    void placeOrder_success() {
        // Arrange
        User user = user(1L);
        Cart cart = new Cart(user);
        cart.getItems().add(new CartItem(cart, product(1L, "Ga ran", "35000", true), 2));
        cart.getItems().add(new CartItem(cart, product(2L, "Burger", "45000", true), 1));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Order order = orderService.placeOrder(user, checkoutForm());

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotalPrice()).isEqualByComparingTo("115000");   // 35.000*2 + 45.000
        assertThat(order.getDeliveryAddress()).isEqualTo("123 Duong ABC");
        assertThat(order.getPhone()).isEqualTo("0912345678");
        assertThat(cart.getItems()).isEmpty();
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Giá được chốt tại thời điểm đặt: đổi giá món sau đó không ảnh hưởng đơn")
    void placeOrder_locksPriceAtOrderTime() {
        User user = user(1L);
        Product chicken = product(1L, "Ga ran", "35000", true);
        Cart cart = new Cart(user);
        cart.getItems().add(new CartItem(cart, chicken, 2));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.placeOrder(user, checkoutForm());
        chicken.setPrice(new BigDecimal("99000"));       // admin đổi giá SAU khi khách đã đặt

        assertThat(order.getItems().get(0).getPriceAtOrder()).isEqualByComparingTo("35000");
        assertThat(order.getTotalPrice()).isEqualByComparingTo("70000");
    }

    @Test
    @DisplayName("Giỏ hàng trống thì không tạo đơn")
    void placeOrder_emptyCart_throws() {
        User user = user(1L);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(new Cart(user)));

        assertThatThrownBy(() -> orderService.placeOrder(user, checkoutForm()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("checkout.emptyCart");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chưa có giỏ hàng thì không tạo đơn")
    void placeOrder_noCart_throws() {
        User user = user(1L);
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(user, checkoutForm()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("checkout.emptyCart");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Giỏ có món đã bị ẩn thì từ chối và giữ nguyên giỏ")
    void placeOrder_unavailableProduct_throwsAndKeepsCart() {
        User user = user(1L);
        Cart cart = new Cart(user);
        cart.getItems().add(new CartItem(cart, product(1L, "Ga ran", "35000", false), 1));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.placeOrder(user, checkoutForm()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("order.productUnavailable");

        assertThat(cart.getItems()).hasSize(1);          // giỏ không bị mất
        verify(orderRepository, never()).save(any());
    }

    // ===== cancel =====

    @Test
    @DisplayName("Khách hủy được đơn đang chờ xác nhận")
    void cancel_pendingOrder_becomesCancelled() {
        User user = user(1L);
        Order order = orderOf(10L, user, OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        orderService.cancel(10L, user);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Đơn đã được xác nhận thì khách không hủy được")
    void cancel_confirmedOrder_throws() {
        User user = user(1L);
        Order order = orderOf(10L, user, OrderStatus.CONFIRMED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(10L, user))
                .isInstanceOf(BusinessException.class)
                .hasMessage("order.cancelOnlyPending");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Không xem hoặc hủy được đơn của người khác")
    void cancel_otherUsersOrder_isNotFound() {
        Order order = orderOf(10L, user(1L), OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(10L, user(2L)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("order.notFound");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    // ===== updateStatus (admin) =====

    @Test
    @DisplayName("Admin chuyển trạng thái đúng thứ tự")
    void updateStatus_validTransition_works() {
        Order order = orderOf(10L, user(1L), OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        orderService.updateStatus(10L, OrderStatus.CONFIRMED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Admin không được nhảy cóc trạng thái")
    void updateStatus_invalidTransition_throws() {
        Order order = orderOf(10L, user(1L), OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(10L, OrderStatus.COMPLETED))
                .isInstanceOf(BusinessException.class);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}