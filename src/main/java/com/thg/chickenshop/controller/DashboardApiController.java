package com.thg.chickenshop.controller;

import com.thg.chickenshop.dto.stats.*;
import com.thg.chickenshop.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/api/stats")
public class DashboardApiController {

    private final DashboardService service;

    public DashboardApiController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public StatsSummary summary(@RequestParam(defaultValue = "TODAY") StatsPeriod period) {
        return service.summary(period);
    }

    @GetMapping("/revenue-by-day")
    public List<DayRevenue> revenueByDay(@RequestParam(defaultValue = "7") int days) {
        return service.revenueByDay(days);
    }

    @GetMapping("/peak-hours")
    public List<HourCount> peakHours(@RequestParam(defaultValue = "MONTH") StatsPeriod period) {
        return service.peakHours(period);
    }

    @GetMapping("/categories")
    public List<ShareRow> categories(@RequestParam(defaultValue = "MONTH") StatsPeriod period) {
        return service.revenueByCategory(period);
    }

    @GetMapping("/payments")
    public List<ShareRow> payments(@RequestParam(defaultValue = "MONTH") StatsPeriod period) {
        return service.revenueByPayment(period);
    }

    @GetMapping("/top-products")
    public List<TopProductRow> topProducts(@RequestParam(defaultValue = "MONTH") StatsPeriod period,
                                           @RequestParam(defaultValue = "5") int limit) {
        return service.topProducts(period, Math.max(1, Math.min(limit, 20)));
    }

    @GetMapping("/top-customers")
    public List<TopCustomerRow> topCustomers(@RequestParam(defaultValue = "MONTH") StatsPeriod period,
                                             @RequestParam(defaultValue = "5") int limit) {
        return service.topCustomers(period, Math.max(1, Math.min(limit, 20)));
    }

    @GetMapping("/low-stock")
    public List<LowStockRow> lowStock(@RequestParam(defaultValue = "10") int threshold) {
        return service.lowStock(threshold);
    }
}