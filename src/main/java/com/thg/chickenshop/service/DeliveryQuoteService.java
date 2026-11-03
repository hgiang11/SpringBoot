package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.DeliveryEstimate;
import com.thg.chickenshop.dto.DeliveryQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DeliveryQuoteService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryQuoteService.class);

    private final DistanceService distanceService;
    private final int prepMinutes;
    private final double shopLat;
    private final double shopLng;

    public DeliveryQuoteService(DistanceService distanceService,
                                @Value("${shop.prep-minutes:15}") int prepMinutes,
                                @Value("${shop.latitude:21.0285}") double shopLat,
                                @Value("${shop.longitude:105.8542}") double shopLng) {
        this.distanceService = distanceService;
        this.prepMinutes = prepMinutes;
        this.shopLat = shopLat;
        this.shopLng = shopLng;
    }

    public DeliveryQuote quote(Double lat, Double lng, String address) {
        boolean valid = lat != null && lng != null
                && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
        Double la = valid ? lat : null;
        Double ln = valid ? lng : null;

        DeliveryEstimate estimate;
        try {
            estimate = distanceService.estimate(la, ln, address);
        } catch (RuntimeException ex) {
            // Google lỗi, hết hạn mức hoặc sai khóa: vẫn báo được con số thay vì làm hỏng đơn
            log.warn("Không tính được quãng đường bằng nguồn chính, dùng ước lượng: {}", ex.getMessage());
            estimate = GeoMath.estimate(shopLat, shopLng, la, ln, address);
        }
        return buildQuote(estimate, prepMinutes, LocalDateTime.now());
    }

    // Hàm thuần: nhận dữ liệu vào, trả kết quả ra, dễ viết test
    static DeliveryQuote buildQuote(DeliveryEstimate estimate, int prepMinutes, LocalDateTime now) {
        // 2 km đầu 15.000 ₫, mỗi km (tính tròn lên) vượt thêm 5.000 ₫
        double extraKm = Math.max(0, estimate.distanceKm() - 2);
        long fee = 15000 + 5000L * (long) Math.ceil(extraKm);
        int total = prepMinutes + estimate.travelMinutes();
        return new DeliveryQuote(estimate.distanceKm(), BigDecimal.valueOf(fee), total, now.plusMinutes(total));
    }
}