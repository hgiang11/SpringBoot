package com.thg.chickenshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime orderDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(precision = 12, scale = 0)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    private String deliveryAddress;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20, columnDefinition = "varchar(20) default 'COD'")
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Column(length = 100)
    private String receiverName;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private Double distanceKm;

    @Column(precision = 12, scale = 0)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    private Integer estimatedMinutes;

    private LocalDateTime estimatedDeliveryTime;

    // [MỚI] Tọa độ điểm giao
    private Double deliveryLat;
    private Double deliveryLng;

    // Tổng khách phải trả = tiền hàng + phí giao. Không đổi totalPrice để dashboard và test cũ không lệch
    public BigDecimal getGrandTotal() {
        return totalPrice.add(shippingFee == null ? BigDecimal.ZERO : shippingFee);
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
