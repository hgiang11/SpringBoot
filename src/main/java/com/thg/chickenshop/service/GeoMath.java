package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.DeliveryEstimate;

/** Tính toán không cần mạng: dùng làm nguồn mô phỏng và làm phương án dự phòng. */
public final class GeoMath {

    private static final double EARTH_KM = 6371.0;
    private static final double ROAD_FACTOR = 1.3;     // đường thật dài hơn đường chim bay
    private static final double SPEED_KMH = 25.0;      // tốc độ trung bình xe máy trong phố

    private GeoMath() { }

    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static DeliveryEstimate estimate(double shopLat, double shopLng,
                                            Double lat, Double lng, String address) {
        double km;
        if (lat != null && lng != null) {
            km = haversineKm(shopLat, shopLng, lat, lng) * ROAD_FACTOR;
        } else {
            // Không có tọa độ: cùng một địa chỉ luôn cho cùng kết quả (2.0 - 9.0 km), chỉ để demo
            String a = address == null ? "" : address.trim().toLowerCase();
            km = 2.0 + (Math.abs(a.hashCode()) % 71) / 10.0;
        }
        km = Math.round(km * 10) / 10.0;
        int minutes = (int) Math.ceil(km / SPEED_KMH * 60);
        return new DeliveryEstimate(km, Math.max(minutes, 1), false);
    }
}