package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.FurnitureStatus;
import com.blashape.backend_blashape.entitys.FurnitureType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestFurniture {
    private Long furnitureId;
    private String name;
    private String documentUrl;
    private String imageInitUrl;
    private String imageEndUrl;
    private String creationDate;
    private String endDate;
    private FurnitureStatus status;
    private FurnitureType type;
    private CuttingDTO cutting;
    private Long carpenterId;
    private Long customerId;
}
