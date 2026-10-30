package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.stats.*;
import com.thg.chickenshop.model.OrderStatus;
import com.thg.chickenshop.repository.StatsRepository;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    // "Thành công" trong dự án này là COMPLETED
    private static final OrderStatus SUCCESS = OrderStatus.COMPLETED;

    private final StatsRepository stats;

    public DashboardService(StatsRepository stats) {
        this.stats = stats;
    }

    // ===== 1. Thẻ số liệu =====
    public StatsSummary summary(StatsPeriod period) {
        DateRange r = rangeOf(period, LocalDate.now());
        return buildSummary(stats.findOrderPoints(r.from(), r.to()));
    }

    // ===== 2. Biểu đồ =====
    public List<DayRevenue> revenueByDay(int days) {
        int n = Math.max(1, Math.min(days, 31));
        LocalDate today = LocalDate.now();
        LocalDate first = today.minusDays(n - 1L);
        List<OrderPoint> points = stats.findOrderPoints(
                first.atStartOfDay(), today.plusDays(1).atStartOfDay());
        return buildDailyRevenue(points, first, n);
    }

    public List<HourCount> peakHours(StatsPeriod period) {
        DateRange r = rangeOf(period, LocalDate.now());
        return buildHourCounts(stats.findOrderPoints(r.from(), r.to()));
    }

    public List<ShareRow> revenueByCategory(StatsPeriod period) {
        DateRange r = rangeOf(period, LocalDate.now());
        String lang = LocaleContextHolder.getLocale().getLanguage();
        return stats.revenueByCategory(SUCCESS, r.from(), r.to()).stream()
                .map(c -> new ShareRow(
                        "en".equals(lang) && c.nameEn() != null && !c.nameEn().isBlank()
                                ? c.nameEn() : c.name(),
                        c.revenue()))
                .toList();
    }

    public List<ShareRow> revenueByPayment(StatsPeriod period) {
        DateRange r = rangeOf(period, LocalDate.now());
        return stats.revenueByPayment(SUCCESS, r.from(), r.to()).stream()
                .map(p -> new ShareRow(p.method().getLabel(), p.revenue()))
                .toList();
    }

    // ===== 3. Bảng =====
    public List<TopProductRow> topProducts(StatsPeriod period, int limit) {
        DateRange r = rangeOf(period, LocalDate.now());
        return stats.topProducts(SUCCESS, r.from(), r.to(), PageRequest.of(0, limit));
    }

    public List<TopCustomerRow> topCustomers(StatsPeriod period, int limit) {
        DateRange r = rangeOf(period, LocalDate.now());
        return stats.topCustomers(SUCCESS, r.from(), r.to(), PageRequest.of(0, limit));
    }

    public List<LowStockRow> lowStock(int threshold) {
        return stats.lowStock(threshold);
    }

    // ===== Hàm tính toán thuần (static, không đụng DB nên dễ viết test) =====

    static DateRange rangeOf(StatsPeriod period, LocalDate today) {
        return switch (period) {
            case TODAY -> new DateRange(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            case WEEK -> {
                LocalDate monday = today.with(DayOfWeek.MONDAY);
                yield new DateRange(monday.atStartOfDay(), monday.plusWeeks(1).atStartOfDay());
            }
            case MONTH -> {
                LocalDate first = today.withDayOfMonth(1);
                yield new DateRange(first.atStartOfDay(), first.plusMonths(1).atStartOfDay());
            }
        };
    }

    static StatsSummary buildSummary(List<OrderPoint> points) {
        long total = points.size();
        long completed = points.stream().filter(p -> p.status() == SUCCESS).count();
        long cancelled = points.stream().filter(p -> p.status() == OrderStatus.CANCELLED).count();

        BigDecimal revenue = points.stream()
                .filter(p -> p.status() == SUCCESS)
                .map(OrderPoint::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double rate = total == 0 ? 0 : Math.round(cancelled * 1000.0 / total) / 10.0;
        BigDecimal aov = completed == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(completed), 0, RoundingMode.HALF_UP);

        return new StatsSummary(revenue, total, completed, cancelled, rate, aov);
    }

    static List<DayRevenue> buildDailyRevenue(List<OrderPoint> points, LocalDate first, int days) {
        // Tạo sẵn mọi ngày với doanh thu 0 để ngày không có đơn vẫn hiện trên biểu đồ
        Map<LocalDate, BigDecimal> byDay = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            byDay.put(first.plusDays(i), BigDecimal.ZERO);
        }
        for (OrderPoint p : points) {
            if (p.status() == SUCCESS) {
                byDay.merge(p.orderDate().toLocalDate(), p.totalPrice(), BigDecimal::add);
            }
        }
        List<DayRevenue> result = new ArrayList<>();
        byDay.forEach((d, v) -> result.add(new DayRevenue(d, v)));
        return result;
    }

    static List<HourCount> buildHourCounts(List<OrderPoint> points) {
        long[] counts = new long[24];
        for (OrderPoint p : points) {
            if (p.status() != OrderStatus.CANCELLED) {
                counts[p.orderDate().getHour()]++;
            }
        }
        List<HourCount> result = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            result.add(new HourCount(h, counts[h]));
        }
        return result;
    }
}