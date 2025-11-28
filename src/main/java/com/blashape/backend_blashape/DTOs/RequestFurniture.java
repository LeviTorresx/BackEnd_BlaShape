package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.FurnitureStatus;
import com.blashape.backend_blashape.entitys.FurnitureType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestFurniture {
    private Long furnitureId;
    private String name;
    private MultipartFile document;
    private MultipartFile imageInit;
    private MultipartFile imageEnd;
    private String creationDate;
    private String endDate;
    private FurnitureStatus status;
    private FurnitureType type;
    private CuttingDTO cutting;
    private Long carpenterId;
    private Long customerId;
}
