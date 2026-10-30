package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.stats.*;
import com.thg.chickenshop.model.OrderStatus;
import com.thg.chickenshop.repository.StatsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock StatsRepository stats;
    @InjectMocks DashboardService service;

    private OrderPoint point(String dateTime, long price, OrderStatus status) {
        return new OrderPoint(LocalDateTime.parse(dateTime), BigDecimal.valueOf(price), status);
    }

    @Test
    void rangeOf_week_startsOnMonday() {
        // 2026-10-30 là thứ Sáu
        DateRange r = DashboardService.rangeOf(StatsPeriod.WEEK, LocalDate.of(2026, 10, 30));
        assertThat(r.from()).isEqualTo(LocalDateTime.of(2026, 10, 26, 0, 0));
        assertThat(r.to()).isEqualTo(LocalDateTime.of(2026, 11, 2, 0, 0));
    }

    @Test
    void rangeOf_month_coversWholeMonth() {
        DateRange r = DashboardService.rangeOf(StatsPeriod.MONTH, LocalDate.of(2026, 10, 30));
        assertThat(r.from()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
        assertThat(r.to()).isEqualTo(LocalDateTime.of(2026, 11, 1, 0, 0));
    }

    @Test
    void buildSummary_countsOnlyCompletedForRevenue() {
        List<OrderPoint> points = List.of(
                point("2026-10-30T10:00:00", 100000, OrderStatus.COMPLETED),
                point("2026-10-30T11:00:00", 50000, OrderStatus.COMPLETED),
                point("2026-10-30T12:00:00", 30000, OrderStatus.CANCELLED),
                point("2026-10-30T13:00:00", 70000, OrderStatus.PENDING));

        StatsSummary s = DashboardService.buildSummary(points);

        assertThat(s.revenue()).isEqualByComparingTo("150000");
        assertThat(s.totalOrders()).isEqualTo(4);
        assertThat(s.completedOrders()).isEqualTo(2);
        assertThat(s.cancelledOrders()).isEqualTo(1);
        assertThat(s.cancellationRate()).isEqualTo(25.0);
        assertThat(s.averageOrderValue()).isEqualByComparingTo("75000");
    }

    @Test
    void buildSummary_emptyList_givesZeros() {
        StatsSummary s = DashboardService.buildSummary(List.of());

        assertThat(s.revenue()).isEqualByComparingTo("0");
        assertThat(s.cancellationRate()).isZero();
        assertThat(s.averageOrderValue()).isEqualByComparingTo("0");
    }

    @Test
    void buildDailyRevenue_fillsDaysWithoutOrders() {
        LocalDate first = LocalDate.of(2026, 10, 28);
        List<OrderPoint> points = List.of(
                point("2026-10-28T09:00:00", 40000, OrderStatus.COMPLETED),
                point("2026-10-28T20:00:00", 60000, OrderStatus.COMPLETED),
                point("2026-10-30T10:00:00", 90000, OrderStatus.COMPLETED),
                point("2026-10-29T10:00:00", 99999, OrderStatus.CANCELLED));

        List<DayRevenue> days = DashboardService.buildDailyRevenue(points, first, 3);

        assertThat(days).hasSize(3);
        assertThat(days.get(0).revenue()).isEqualByComparingTo("100000");
        assertThat(days.get(1).revenue()).isEqualByComparingTo("0");
        assertThat(days.get(2).revenue()).isEqualByComparingTo("90000");
    }

    @Test
    void peakHours_groupsByHourAndSkipsCancelled() {
        when(stats.findOrderPoints(any(), any())).thenReturn(List.of(
                point("2026-10-30T12:10:00", 1, OrderStatus.COMPLETED),
                point("2026-10-30T12:40:00", 1, OrderStatus.PENDING),
                point("2026-10-30T12:55:00", 1, OrderStatus.CANCELLED),
                point("2026-10-30T19:05:00", 1, OrderStatus.COMPLETED)));

        List<HourCount> hours = service.peakHours(StatsPeriod.TODAY);

        assertThat(hours).hasSize(24);
        assertThat(hours.get(12).orders()).isEqualTo(2);
        assertThat(hours.get(19).orders()).isEqualTo(1);
        assertThat(hours.get(3).orders()).isZero();
    }
}