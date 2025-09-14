package com.blashape.backend_blashape.entitys;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "furniture")
public class Furniture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long furnitureId;

    private String name;
    private String documentURL;
    private String imageInitURL;
    private String imageEndURL;
    private LocalDate creationDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private FurnitureStatus status;

    @ManyToOne
    @JoinColumn(name = "carpenter_id", referencedColumnName = "carpenterId")
    @JsonBackReference(value = "carpenter-furniture")
    private Carpenter carpenter;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    @JsonBackReference(value = "customer-furniture")
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "furniture_id")
    private List<Piece> pieces;

}
