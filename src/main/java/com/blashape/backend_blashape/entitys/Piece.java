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
public class Piece extends Sheet {

    private int quantity;

    @Embedded
    private Edges edges;

    @ManyToOne
    @JoinColumn(name = "furniture_id", referencedColumnName = "furnitureId")
    private Furniture furniture;
}
