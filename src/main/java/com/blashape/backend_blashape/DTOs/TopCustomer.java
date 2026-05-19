package com.blashape.backend_blashape.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomer {
    private String fullName;
    private String dni;
    private Long furnitureCount;
}
