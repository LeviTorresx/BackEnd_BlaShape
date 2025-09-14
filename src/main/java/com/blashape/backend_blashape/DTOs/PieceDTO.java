package com.blashape.backend_blashape.DTOs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PieceDTO{
    private Long pieceId;
    private int quantity;
    private Double height;
    private Double width;
    private Double thickness;
    private String colorName;
    private String colorHex;
    private String materialName;

    private EdgesDTO edges;

}
