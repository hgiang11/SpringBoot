package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.DeliveryEstimate;
import com.thg.chickenshop.dto.DeliveryQuote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryQuoteServiceTest {

    @Mock DistanceService distanceService;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 19, 0);

    @Test
    void buildQuote_within2km_isBaseFee() {
        DeliveryQuote q = DeliveryQuoteService.buildQuote(new DeliveryEstimate(1.2, 5, false), 15, NOW);
        assertThat(q.shippingFee()).isEqualByComparingTo("15000");
        assertThat(q.totalMinutes()).isEqualTo(20);
        assertThat(q.eta()).isEqualTo(NOW.plusMinutes(20));
    }

    @Test
    void buildQuote_beyond2km_addsFeePerStartedKm() {
        // 5,2 km: 3,2 km vượt, tính tròn lên 4 km => 15.000 + 4 x 5.000
        DeliveryQuote q = DeliveryQuoteService.buildQuote(new DeliveryEstimate(5.2, 20, false), 15, NOW);
        assertThat(q.shippingFee()).isEqualByComparingTo("35000");
        assertThat(q.totalMinutes()).isEqualTo(35);
    }

    @Test
    void quote_fallsBackWhenPrimaryProviderFails() {
        when(distanceService.estimate(any(), any(), anyString())).thenThrow(new IllegalStateException("loi mang"));
        DeliveryQuoteService service = new DeliveryQuoteService(distanceService, 15, 21.0285, 105.8542);

        DeliveryQuote q = service.quote(21.0400, 105.8600, "abc");

        assertThat(q.distanceKm()).isGreaterThan(0);
        assertThat(q.totalMinutes()).isGreaterThanOrEqualTo(16);
    }

    @Test
    void geoMath_sameAddressGivesSameEstimate_andStaysInRange() {
        DeliveryEstimate a = GeoMath.estimate(21.0285, 105.8542, null, null, "12 Nguyen Trai");
        DeliveryEstimate b = GeoMath.estimate(21.0285, 105.8542, null, null, "12 Nguyen Trai");
        assertThat(a).isEqualTo(b);
        assertThat(a.distanceKm()).isBetween(2.0, 9.1);
    }

    @Test
    void geoMath_haversine_hanoiToHcmIsAbout1100To1200Km() {
        double km = GeoMath.haversineKm(21.0285, 105.8542, 10.7769, 106.7009);
        assertThat(km).isBetween(1100.0, 1200.0);
    }
}