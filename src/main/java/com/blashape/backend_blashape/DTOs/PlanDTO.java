package com.blashape.backend_blashape.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanDTO {
    private String planName;
    private Long price;
    private String currency;
    private String stripePriceId;
    private Integer cuttingLimit;
    private Boolean svg;
    private Boolean limitedSvg;
    private Boolean pdf;
    private Boolean limitedRecord;
    private Integer duration;
    private Boolean meaningPieces;
    private Boolean analyticsModule;
    private Boolean businessLicence;
}
