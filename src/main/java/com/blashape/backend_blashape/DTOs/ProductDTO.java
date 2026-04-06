package com.blashape.backend_blashape.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String productName;
    private String description;
    private Long price;
    private String currency;
    private String stripeProductId;
}
