package com.blashape.backend_blashape.DTOs;
import com.blashape.backend_blashape.entitys.Color;
import com.blashape.backend_blashape.entitys.Edges;
import com.blashape.backend_blashape.entitys.Piece;
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
    private String name;
    private int quantity;
    private Double height;
    private Double width;
    private Double thickness;
    private ColorDTO colorDTO;
    private String materialName;
    private EdgesDTO edgesDTO;
    private boolean rotationAllowed = true;

    public Piece toModel() {
        Piece p = new Piece();
        p.setPieceId(pieceId);
        p.setName(name);
        p.setWidth(width);
        p.setHeight(height);
        p.setQuantity(quantity);
        p.setThickness(thickness);
        p.setMaterialName(materialName);

        Color color = new Color();

        color.setName(colorDTO.getName());
        color.setHex(colorDTO.getHex());

        p.setColor(color);

        Edges edges = new Edges();
        edges.setTop(edgesDTO.getTop());
        edges.setBottom(edgesDTO.getBottom());
        edges.setLeft(edgesDTO.getLeft());
        edges.setRight(edgesDTO.getRight());

        p.setEdges(edges);

        p.setRotationAllowed(rotationAllowed);
        return p;
    }
}
