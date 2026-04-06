package com.blashape.backend_blashape.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blashape.backend_blashape.services.StripeService;

@RestController
@RequestMapping("/api_BS/stripe")
public class StripeController {
    @Autowired
    private StripeService stripeService;

    @PostMapping("/create-product/{productId}")
    public ResponseEntity<String> createProduct(@PathVariable Long productId) throws Exception {
        return ResponseEntity.ok(stripeService.createProductStripe(productId));
    }

    @PostMapping("/create-plan/{planId}")
    public ResponseEntity<String> createPlan(@PathVariable Long planId) throws Exception {
        return ResponseEntity.ok(stripeService.createSubscriptionPlanStripe(planId));
    }
}
