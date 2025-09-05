package com.blashape.backend_blashape.DTOs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopDTO {
    private Long workshopId;
    private String address;
    private String nit;
    private String phone;
    private String name;
    private Long carpenterId;
}
