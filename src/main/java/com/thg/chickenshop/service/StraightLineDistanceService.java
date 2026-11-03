package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.DeliveryEstimate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "distance.provider", havingValue = "straight-line", matchIfMissing = true)
public class StraightLineDistanceService implements DistanceService {

    private final double shopLat;
    private final double shopLng;

    public StraightLineDistanceService(@Value("${shop.latitude:21.0285}") double shopLat,
                                       @Value("${shop.longitude:105.8542}") double shopLng) {
        this.shopLat = shopLat;
        this.shopLng = shopLng;
    }

    @Override
    public DeliveryEstimate estimate(Double lat, Double lng, String address) {
        return GeoMath.estimate(shopLat, shopLng, lat, lng, address);
    }
}