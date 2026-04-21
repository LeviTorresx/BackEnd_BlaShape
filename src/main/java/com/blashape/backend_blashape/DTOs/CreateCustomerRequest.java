package com.blashape.backend_blashape.DTOs;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomerRequest {
    private Long customerId;
    private String name;
    private String lastName;
    private String dni;
    private String  phone;
    private String email;
    private String role;
    private Boolean isActive;
    private Instant deletedAt;
    private Long carpenterId;
    private List<Long> furnitureListIds;
}
