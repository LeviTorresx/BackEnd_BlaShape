package com.blashape.backend_blashape.entitys;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subscriptions")
public class AppSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;

    @ManyToOne
    private Carpenter carpenter;

    @ManyToOne
    private Plan plan;

    private String stripeSubscriptionId;

    private String stripeCustomerId;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private Instant startDate;
    
    private Instant endDate;

    private Integer remainingCuts;
}
