package com.blashape.backend_blashape.DTOs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long customerId;
    private String name;
    private String lastName;
    private String dni;
    private String  phone;
    private String email;
    private String role;

    private List<Long> furnitureListIds;

}
