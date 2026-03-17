package com.blashape.backend_blashape.entitys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuttingPosition {

    private Piece piece;
    private Double x;
    private Double y;
    private boolean rotated; // rotation 90


    public double getEffectiveWidth(){ return rotated ? piece.getHeight() : piece.getWidth();}
    public double getEffectiveHeight(){ return rotated ? piece.getWidth() : piece.getHeight();}

    /**
     * Indica si él tapa canto de ancho-1 (cara Y=0 de la pieza original)
     * está activo, corrigiendo por rotación.
     */
    public boolean isEdgeBandingX1(){ return rotated ? piece.getEdges().getRight() : piece.getEdges().getTop();}
    public boolean isEdgeBandingX2(){ return rotated ? piece.getEdges().getLeft() : piece.getEdges().getBottom();}
    public boolean isEdgeBandingY1(){ return rotated ? piece.getEdges().getTop() : piece.getEdges().getRight();}
    public boolean isEdgeBandingY2(){ return rotated ? piece.getEdges().getBottom() : piece.getEdges().getLeft();}

}
