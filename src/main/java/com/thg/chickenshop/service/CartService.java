package com.thg.chickenshop.service;

import com.thg.chickenshop.exception.BusinessException;
import com.thg.chickenshop.exception.NotFoundException;
import com.thg.chickenshop.model.Cart;
import com.thg.chickenshop.model.CartItem;
import com.thg.chickenshop.model.Product;
import com.thg.chickenshop.model.User;
import com.thg.chickenshop.repository.CartRepository;
import com.thg.chickenshop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CartService {

    private static final int MAX_QUANTITY = 20;

    /**
     * Kết quả trả về cho AJAX sau khi cập nhật / xóa.
     * subtotal: thành tiền của dòng vừa sửa (null nếu dòng đã bị xóa)
     * total: tổng cộng cả giỏ
     * totalQuantity: tổng số phần trong giỏ
     */
    public record CartUpdateResult(Long itemId, BigDecimal subtotal, BigDecimal total, int totalQuantity) {}

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional
    public void addItem(User user, Long productId, int quantity) {
        if (quantity < 1) {
            throw new BusinessException("Số lượng phải từ 1 trở lên");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy món ăn"));
        if (!product.isAvailable()) {
            throw new BusinessException("Món này hiện không còn bán");
        }

        Cart cart = getOrCreateCart(user);
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existing != null) {                       // đã có trong giỏ: cộng dồn
            int newQty = existing.getQuantity() + quantity;
            if (newQty > MAX_QUANTITY) {
                throw new BusinessException("Mỗi món tối đa " + MAX_QUANTITY + " phần trong giỏ");
            }
            existing.setQuantity(newQty);
        } else {                                      // chưa có: thêm dòng mới
            if (quantity > MAX_QUANTITY) {
                throw new BusinessException("Mỗi món tối đa " + MAX_QUANTITY + " phần trong giỏ");
            }
            cart.getItems().add(new CartItem(cart, product, quantity));
        }
    }

    @Transactional
    public CartUpdateResult updateQuantity(User user, Long itemId, int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new BusinessException("Số lượng phải từ 1 đến " + MAX_QUANTITY);
        }
        Cart cart = getOrCreateCart(user);
        CartItem item = findItem(cart, itemId);
        item.setQuantity(quantity);
        return new CartUpdateResult(itemId, item.getSubtotal(), cart.getTotal(), sumQuantity(cart));
    }

    @Transactional
    public CartUpdateResult removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = findItem(cart, itemId);
        cart.getItems().remove(item);       // orphanRemoval = true: dòng này bị xóa khỏi DB
        return new CartUpdateResult(itemId, null, cart.getTotal(), sumQuantity(cart));
    }

    @Transactional(readOnly = true)
    public int countItems(User user) {
        return cartRepository.findByUser(user)
                .map(c -> c.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotal(User user) {
        return cartRepository.findByUser(user)
                .map(Cart::getTotal)
                .orElse(BigDecimal.ZERO);
    }

    private int sumQuantity(Cart cart) {
        return cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Chỉ tìm trong giỏ CỦA CHÍNH NGƯỜI DÙNG này
    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Không tìm thấy món trong giỏ hàng"));
    }
}