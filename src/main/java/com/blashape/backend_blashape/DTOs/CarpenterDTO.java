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
public class CarpenterDTO {
    private Long carpenterId;
    private String name;
    private String lastName;
    private String dni;
    private String rut;
    private String email;
    private String password;
    private String phone;
    private String role;

    private WorkshopDTO workshop;
    private List<Long> furnitureListIds;
}
