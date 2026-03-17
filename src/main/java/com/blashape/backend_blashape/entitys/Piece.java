package com.blashape.backend_blashape.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "piece")
public class Piece{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pieceId;
    private int quantity;
    private String name;
    private double height;
    private double width;
    private Double thickness;
    private String materialName;
    private Boolean rotationAllowed;

    @Embedded
    private Color color;

    @Embedded
    private Edges edges;

    @ManyToOne
    @JoinColumn(name = "cutting_id")
    private Cutting cutting;

    public double  getAreaMm2() {return width*height;}

    public int amountEdgeBanding (){
        int count = 0;
        if (edges.getTop()) count++;
        if (edges.getBottom()) count++;
        if (edges.getLeft()) count++;
        if (edges.getRight()) count++;
        return count;

    }

}
