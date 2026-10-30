package com.thg.chickenshop.repository;

import com.thg.chickenshop.dto.stats.*;
import com.thg.chickenshop.model.Order;
import com.thg.chickenshop.model.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends Repository<Order, Long> {

    // Các đơn trong khoảng thời gian: dùng cho thẻ số liệu, doanh thu theo ngày, giờ cao điểm
    @Query("""
            select new com.thg.chickenshop.dto.stats.OrderPoint(o.orderDate, o.totalPrice, o.status)
            from Order o
            where o.orderDate >= :from and o.orderDate < :to
            """)
    List<OrderPoint> findOrderPoints(@Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);

    // Top món bán chạy (chỉ tính đơn đã hoàn thành)
    @Query("""
            select new com.thg.chickenshop.dto.stats.TopProductRow(
                p.id, p.name, p.imageUrl, sum(i.quantity), sum(i.priceAtOrder * i.quantity))
            from OrderItem i join i.product p join i.order o
            where o.status = :status and o.orderDate >= :from and o.orderDate < :to
            group by p.id, p.name, p.imageUrl
            order by sum(i.quantity) desc
            """)
    List<TopProductRow> topProducts(@Param("status") OrderStatus status,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to,
                                    Pageable pageable);

    // Top khách chi nhiều nhất
    @Query("""
            select new com.thg.chickenshop.dto.stats.TopCustomerRow(
                u.id, u.fullName, u.phone, count(o), sum(o.totalPrice))
            from Order o join o.user u
            where o.status = :status and o.orderDate >= :from and o.orderDate < :to
            group by u.id, u.fullName, u.phone
            order by sum(o.totalPrice) desc
            """)
    List<TopCustomerRow> topCustomers(@Param("status") OrderStatus status,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to,
                                      Pageable pageable);

    // Doanh thu theo danh mục
    @Query("""
            select new com.thg.chickenshop.dto.stats.CategoryRevenueRow(
                c.name, c.nameEn, sum(i.priceAtOrder * i.quantity))
            from OrderItem i join i.product p join p.category c join i.order o
            where o.status = :status and o.orderDate >= :from and o.orderDate < :to
            group by c.id, c.name, c.nameEn
            order by sum(i.priceAtOrder * i.quantity) desc
            """)
    List<CategoryRevenueRow> revenueByCategory(@Param("status") OrderStatus status,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    // Tỷ lệ phương thức thanh toán
    @Query("""
            select new com.thg.chickenshop.dto.stats.PaymentRow(
                o.paymentMethod, count(o), sum(o.totalPrice))
            from Order o
            where o.status = :status and o.orderDate >= :from and o.orderDate < :to
            group by o.paymentMethod
            """)
    List<PaymentRow> revenueByPayment(@Param("status") OrderStatus status,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);

    // Món sắp hết hàng
    @Query("""
            select new com.thg.chickenshop.dto.stats.LowStockRow(p.id, p.name, p.stock)
            from Product p
            where p.available = true and p.stock < :threshold
            order by p.stock asc
            """)
    List<LowStockRow> lowStock(@Param("threshold") int threshold);
}