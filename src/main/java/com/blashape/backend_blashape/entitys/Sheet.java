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
@Table(name ="sheets")
@Inheritance(strategy = InheritanceType.JOINED)
public class Sheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sheetId;
    private Double height;
    private Double width;

    @ManyToOne
    @JoinColumn(name = "material_id", referencedColumnName = "materialId")
    private Material material;
}
