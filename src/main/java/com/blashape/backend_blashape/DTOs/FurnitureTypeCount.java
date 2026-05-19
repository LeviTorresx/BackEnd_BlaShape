package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.FurnitureType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureTypeCount {
    private FurnitureType furnitureType;
    private Long count;
}
