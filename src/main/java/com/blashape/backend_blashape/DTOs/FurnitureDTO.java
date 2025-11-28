package com.blashape.backend_blashape.DTOs;
import com.blashape.backend_blashape.entitys.FurnitureStatus;
import com.blashape.backend_blashape.entitys.FurnitureType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureDTO {
    private Long furnitureId;
    private String name;
    private String documentURL;
    private String imageInitURL;
    private String imageEndURL;
    private LocalDate creationDate;
    private LocalDate endDate;
    private FurnitureStatus status;
    private FurnitureType type;
    private CuttingDTO cutting;
    private Long carpenterId;
    private Long customerId;
}
