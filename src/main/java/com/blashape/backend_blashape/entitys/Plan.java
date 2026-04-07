package com.blashape.backend_blashape.entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(nullable = false)
    private String planName;

    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanInterval interval;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private String currency;

    private String stripePriceId;

    @Column(nullable = false)
    private Integer cuttingLimit;

    @Column(nullable = false)
    private Boolean svg;

    @Column(nullable = false)
    private Boolean limitedSvg;

    @Column(nullable = false)
    private Boolean pdf;

    @Column(nullable = false)
    private Boolean limitedRecord;

    @Column(nullable = false)
    private Boolean meaningPieces;

    @Column(nullable = false)
    private Boolean analyticsModule;

    @Column(nullable = false)
    private Boolean businessLicence;
}
