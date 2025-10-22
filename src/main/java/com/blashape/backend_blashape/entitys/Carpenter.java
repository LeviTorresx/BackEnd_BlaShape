package com.blashape.backend_blashape.entitys;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="carpenter")
public class Carpenter extends User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long carpenterId;

    private String rut;
    private String password;

    @OneToOne(mappedBy = "carpenter", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Workshop workshop;

    @OneToMany(mappedBy = "carpenter", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "carpenter-furniture")
    private List<Furniture> furnitureList;

    @OneToMany(mappedBy = "carpenter", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "carpenter-alert")
    private List<Alert> alertList;

    @OneToMany(mappedBy = "carpenter", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "carpenter-customer")
    private List<Customer> customers;
}
