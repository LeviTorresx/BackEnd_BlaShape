package com.blashape.backend_blashape.entitys;

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

    @OneToOne
    @JoinColumn(name="furniture_id")
    private Furniture furniture;

    @OneToMany(mappedBy = "cutting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Piece> pieces;
}
