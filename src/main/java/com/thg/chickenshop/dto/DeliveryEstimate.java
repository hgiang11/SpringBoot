package com.thg.chickenshop.dto;

public record DeliveryEstimate(double distanceKm, int travelMinutes, boolean fromGoogle) {}