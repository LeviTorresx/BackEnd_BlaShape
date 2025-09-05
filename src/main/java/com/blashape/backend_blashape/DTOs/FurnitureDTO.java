package com.blashape.backend_blashape.DTOs;
import com.blashape.backend_blashape.entitys.FurnitureStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureDTO {
    private Long furnitureId;
    private String name;
    private String documentUrl;
    private String imagenInitUrl;
    private String imageEndUrl;
    private FurnitureStatus status;
    private Long carpenterId;
    private Long customerId;
    private List<Long> piecesIds;
}
