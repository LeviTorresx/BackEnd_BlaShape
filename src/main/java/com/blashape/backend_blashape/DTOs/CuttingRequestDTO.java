package com.blashape.backend_blashape.DTOs;

import com.blashape.backend_blashape.entitys.Edges;
import com.blashape.backend_blashape.entitys.Material;
import com.blashape.backend_blashape.entitys.Piece;
import com.blashape.backend_blashape.entitys.Sheet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CuttingRequestDTO {
    private String proyect;

    @NotNull(message = "El tablero es obligatorio")
    @Valid
    private SheetDTO sheet;

    @NotEmpty(message = "Debe incluir al menos una pieza")
    @Valid
    private List<PieceDTO> pieces;

    @Data
    public static class SheetDTO {
        private double width;
        private double height;
        private String material;

        public Sheet toModel() {
            Sheet sheet = new Sheet();
            sheet.setWidth(width);
            sheet.setHeight(height);
            Material mat = new Material();
            mat.setName(material);
            sheet.setMaterial(mat);

            return sheet;
        }
    }

    @Data
    public static class PieceDTO {
        private String name;
        private double width;
        private double largo;
        private int    cantidad = 1;
        private boolean edgeTop;
        private boolean edgeBottom;
        private boolean edgeLeft;
        private boolean edgeRight;

        private boolean rotationAllowed = true;

        public Piece toModel() {
            Piece p = new Piece();
            p.setName(name);
            p.setWidth(width);
            p.setHeight(largo);
            p.setQuantity(cantidad);

            Edges edges = new Edges();
            edges.setTop(edgeTop);
            edges.setBottom(edgeBottom);
            edges.setLeft(edgeLeft);
            edges.setRight(edgeRight);

            p.setEdges(edges);

            p.setRotationAllowed(rotationAllowed);
            return p;
        }
    }
}
