package com.thg.chickenshop.controller;

import com.thg.chickenshop.dto.DeliveryQuote;
import com.thg.chickenshop.service.DeliveryQuoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryApiController {

    private final DeliveryQuoteService quoteService;

    public DeliveryApiController(DeliveryQuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/estimate")
    public DeliveryQuote estimate(@RequestParam(required = false) Double lat,
                                  @RequestParam(required = false) Double lng,
                                  @RequestParam(required = false, defaultValue = "") String address) {
        return quoteService.quote(lat, lng, address);
    }
}