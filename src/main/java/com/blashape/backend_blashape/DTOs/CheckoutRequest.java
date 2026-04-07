package com.blashape.backend_blashape.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {
    private Long id;
    private Long carpenterId;
    private String paymentType;
    private String description;
    private String successUrl;
    private String cancelUrl;
}
