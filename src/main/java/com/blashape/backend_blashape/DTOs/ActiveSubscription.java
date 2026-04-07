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
public class ActiveSubscription {
    private PlanDTO plan;
    private SubscriptionStatus status;
    private Instant startDate;
    private Instant endDate;
}
