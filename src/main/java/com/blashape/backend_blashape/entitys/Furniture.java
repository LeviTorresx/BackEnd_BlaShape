package com.blashape.backend_blashape.entitys;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @OneToOne(mappedBy = "furniture", cascade = CascadeType.ALL)
    @JsonManagedReference("furniture-cutting")
    private Cutting cutting;

    public void setCutting(Cutting cutting) {
        this.cutting = cutting;

        // Sincroniza el otro lado
        if (cutting != null && cutting.getFurniture() != this) {
            cutting.setFurniture(this);
        }
    }
}
