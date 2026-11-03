package com.thg.chickenshop.service;

import com.thg.chickenshop.dto.DeliveryEstimate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "distance.provider", havingValue = "google")
public class GoogleDistanceService implements DistanceService {

    private static final String URL = "https://routes.googleapis.com/directions/v2:computeRoutes";

    private final RestClient rest = RestClient.create();
    private final String serverKey;
    private final double shopLat;
    private final double shopLng;

    public GoogleDistanceService(@Value("${google.maps.server-key:}") String serverKey,
                                 @Value("${shop.latitude}") double shopLat,
                                 @Value("${shop.longitude}") double shopLng) {
        this.serverKey = serverKey;
        this.shopLat = shopLat;
        this.shopLng = shopLng;
    }

    @Override
    public DeliveryEstimate estimate(Double lat, Double lng, String address) {
        if (serverKey == null || serverKey.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình google.maps.server-key");
        }
        Map<String, Object> destination;
        if (lat != null && lng != null) {
            destination = Map.of("location", Map.of("latLng", Map.of("latitude", lat, "longitude", lng)));
        } else if (address != null && !address.isBlank()) {
            destination = Map.of("address", address);
        } else {
            throw new IllegalArgumentException("Thiếu tọa độ và địa chỉ");
        }
        Map<String, Object> body = Map.of(
                "origin", Map.of("location", Map.of("latLng", Map.of("latitude", shopLat, "longitude", shopLng))),
                "destination", destination,
                "travelMode", "DRIVE");

        RoutesResponse res = rest.post()
                .uri(URL)
                .header("X-Goog-Api-Key", serverKey)
                .header("X-Goog-FieldMask", "routes.duration,routes.distanceMeters")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(RoutesResponse.class);

        if (res == null || res.routes() == null || res.routes().isEmpty()) {
            throw new IllegalStateException("Google không trả về tuyến đường");
        }
        Route route = res.routes().get(0);
        double km = (route.distanceMeters() == null ? 0 : route.distanceMeters()) / 1000.0;
        double seconds = Double.parseDouble(route.duration().replace("s", ""));
        return new DeliveryEstimate(Math.round(km * 10) / 10.0, (int) Math.ceil(seconds / 60.0), true);
    }

    record RoutesResponse(List<Route> routes) { }

    record Route(Integer distanceMeters, String duration) { }
}