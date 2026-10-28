package com.thg.chickenshop.service;

import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.exception.NotFoundException;
import com.thg.chickenshop.model.Cart;
import com.thg.chickenshop.model.CartItem;
import com.thg.chickenshop.model.Product;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.repository.CartRepository;
import com.thg.chickenshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        cart = new Cart(user);
    }

    private Product product(Long id, String price, boolean available) {
        Product p = new Product();
        p.setId(id);
        p.setName("Mon " + id);
        p.setPrice(new BigDecimal(price));
        p.setAvailable(available);
        return p;
    }

    private CartItem addToCart(Long itemId, Product product, int quantity) {
        CartItem item = new CartItem(cart, product, quantity);
        item.setId(itemId);
        cart.getItems().add(item);
        return item;
    }

    // ===== addItem =====

    @Test
    @DisplayName("Thêm món mới vào giỏ tạo một dòng mới")
    void addItem_newProduct_createsNewLine() {
        Product chicken = product(1L, "35000", true);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(chicken));

        cartService.addItem(user, 1L, 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Thêm món đã có thì cộng dồn số lượng, không tạo dòng mới")
    void addItem_existingProduct_accumulatesQuantity() {
        Product chicken = product(1L, "35000", true);
        addToCart(100L, chicken, 3);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(chicken));

        cartService.addItem(user, 1L, 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cộng dồn vượt 20 phần thì bị từ chối và giữ nguyên số lượng cũ")
    void addItem_exceedsMax_throws() {
        Product chicken = product(1L, "35000", true);
        addToCart(100L, chicken, 19);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(chicken));

        assertThatThrownBy(() -> cartService.addItem(user, 1L, 2))
                .isInstanceOf(BusinessException.class)
                .hasMessage("cart.maxPerItem");

        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(19);
    }

    @Test
    @DisplayName("Không thêm được món đã bị ẩn")
    void addItem_unavailableProduct_throws() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product(1L, "35000", false)));

        assertThatThrownBy(() -> cartService.addItem(user, 1L, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessage("product.unavailable");

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Không thêm được món không tồn tại")
    void addItem_unknownProduct_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(user, 99L, 1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("product.notFound");
    }

    @Test
    @DisplayName("Số lượng nhỏ hơn 1 bị từ chối ngay, không truy cập database")
    void addItem_zeroQuantity_throws() {
        assertThatThrownBy(() -> cartService.addItem(user, 1L, 0))
                .isInstanceOf(BusinessException.class)
                .hasMessage("cart.qtyMin");

        verifyNoInteractions(productRepository);
    }

    // ===== updateQuantity =====

    @Test
    @DisplayName("Sửa số lượng hợp lệ")
    void updateQuantity_valid_updatesLine() {
        CartItem item = addToCart(100L, product(1L, "35000", true), 1);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.updateQuantity(user, 100L, 7);

        assertThat(item.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("Số lượng ngoài khoảng 1 đến 20 bị từ chối")
    void updateQuantity_outOfRange_throws() {
        assertThatThrownBy(() -> cartService.updateQuantity(user, 100L, 21))
                .isInstanceOf(BusinessException.class)
                .hasMessage("cart.qtyRange");
    }

    // ===== removeItem =====

    @Test
    @DisplayName("Xóa đúng dòng trong giỏ của mình")
    void removeItem_ownItem_removesIt() {
        addToCart(100L, product(1L, "35000", true), 1);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.removeItem(user, 100L);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Không xóa được dòng không thuộc giỏ của mình (chống sửa id)")
    void removeItem_itemNotInMyCart_isNotFound() {
        addToCart(100L, product(1L, "35000", true), 1);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.removeItem(user, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("cart.itemNotFound");

        assertThat(cart.getItems()).hasSize(1);
    }

    // ===== countItems, getTotal =====

    @Test
    @DisplayName("Đếm tổng số phần và tính tổng tiền")
    void countAndTotal_areCorrect() {
        addToCart(100L, product(1L, "35000", true), 2);
        addToCart(101L, product(2L, "45000", true), 1);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        assertThat(cartService.countItems(user)).isEqualTo(3);
        assertThat(cartService.getTotal(user)).isEqualByComparingTo("115000");
    }

    @Test
    @DisplayName("Chưa có giỏ thì đếm 0 và tổng 0")
    void countAndTotal_noCart_areZero() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThat(cartService.countItems(user)).isZero();
        assertThat(cartService.getTotal(user)).isEqualByComparingTo("0");
    }
}