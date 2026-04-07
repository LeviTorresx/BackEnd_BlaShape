package com.blashape.backend_blashape.DTOs;

import java.time.Instant;

import com.blashape.backend_blashape.entitys.PaymentStatus;
import com.blashape.backend_blashape.entitys.PaymentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private PaymentType paymentType;
    private ProductDTO product;
    private SubscriptionDTO subscription;
    private PlanDTO plan;
    private String description;
    private CarpenterDTO carpenter;
    private String stripeSessionId;
    private String stripePaymentIntent;
    private Long amount;
    private String currency;
    private PaymentStatus status;
    private Instant createdAt;
}
