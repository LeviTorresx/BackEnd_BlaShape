package com.blashape.backend_blashape.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blashape.backend_blashape.DTOs.CheckoutRequest;
import com.blashape.backend_blashape.entitys.PaymentType;
import com.blashape.backend_blashape.services.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api_BS/stripe")
public class StripeController {
    @Autowired
    private StripeService stripeService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/create-product/{productId}")
    public ResponseEntity<String> createProduct(@PathVariable Long productId) throws Exception {
        return ResponseEntity.ok(stripeService.createProductStripe(productId));
    }

    @PostMapping("/create-plan/{planId}")
    public ResponseEntity<String> createPlan(@PathVariable Long planId) throws Exception {
        return ResponseEntity.ok(stripeService.createSubscriptionPlanStripe(planId));
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<String> createCheckoutSession(@RequestBody CheckoutRequest checkoutRequest) throws StripeException {
        return ResponseEntity.ok(stripeService.createCheckoutSession(stripeService.createPayment(checkoutRequest.getId(), 
                                                                                                checkoutRequest.getCarpenterId(), 
                                                                                                PaymentType.valueOf(checkoutRequest.getPaymentType()), 
                                                                                                checkoutRequest.getDescription()), 
                                                                    checkoutRequest.getSuccessUrl(), 
                                                                    checkoutRequest.getCancelUrl()));
    }

    @PostMapping("/cancel-subscription/{carpenterId}")
    public ResponseEntity<String> cancelSubscription(@PathVariable Long carpenterId) {
        try {
            stripeService.cancelSubscription(carpenterId);
            return ResponseEntity.ok("Suscripción cancelada exitosamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cancelar la suscripción: " + e.getMessage());
        }
    }

    @PostMapping(value = "/webhook", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody byte[] payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(new String(payload, java.nio.charset.StandardCharsets.UTF_8), sigHeader, webhookSecret);
        } catch (Exception e) {
            log.error("Error al verificar la firma del webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Firma errónea.");
        }

        try {
            if ("checkout.session.completed".equals(event.getType())) {
                String rawJson = event.getData().toJson();

                com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();

                String sessionId = mapper.readTree(rawJson)
                        .path("object")
                        .path("id")
                        .asText();

                Session session = Session.retrieve(sessionId);
                stripeService.handleCheckoutSessionCompleted(session);
            }
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error procesando evento.");
        }

        return ResponseEntity.ok("Webhook received successfully.");
    }
}
