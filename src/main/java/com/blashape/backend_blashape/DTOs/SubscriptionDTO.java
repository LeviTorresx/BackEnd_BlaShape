package com.blashape.backend_blashape.DTOs;

import java.time.Instant;

import com.blashape.backend_blashape.entitys.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private Long subscriptionId;
    private CarpenterDTO carpenter;
    private PlanDTO plan;
    private String stripeSubscriptionId;
    private String stripeCustomerId;
    private SubscriptionStatus status;
    private Instant startDate;
    private Instant endDate;
    private Integer remainingCuts;
}
