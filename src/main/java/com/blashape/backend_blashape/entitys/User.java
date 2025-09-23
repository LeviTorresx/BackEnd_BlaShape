package com.blashape.backend_blashape.entitys;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class User {
    private String name;
    private String lastName;
    private String dni;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}
