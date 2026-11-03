package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.DeliveryEstimate;

public interface DistanceService {
    // lat, lng có thể null (khách gõ tay); khi đó dùng address
    DeliveryEstimate estimate(Double lat, Double lng, String address);
}