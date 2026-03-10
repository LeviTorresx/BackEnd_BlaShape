package com.blashape.backend_blashape.entitys;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="cutting")
public class Cutting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cuttingId;
    private String materialName;
    private Integer sheetQuantity;

    @OneToOne
    @JoinColumn(name="furniture_id")
    @JsonBackReference("furniture-cutting")
    private Furniture furniture;
    public void setFurniture(Furniture furniture) {
        this.furniture = furniture;

        // Sincroniza el otro lado
        if (furniture != null && furniture.getCutting() != this) {
            furniture.setCutting(this);
        }
    }

    @OneToMany(mappedBy = "cutting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Piece> pieces;
}
