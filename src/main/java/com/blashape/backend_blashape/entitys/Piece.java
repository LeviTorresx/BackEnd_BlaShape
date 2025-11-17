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
    private Double height;
    private Double width;
    private Double thickness;
    private String materialName;

    @Embedded
    private Color color;

    @Embedded
    private Edges edges;

    @ManyToOne
    @JoinColumn(name = "cutting")
    private Cutting cutting;
}
