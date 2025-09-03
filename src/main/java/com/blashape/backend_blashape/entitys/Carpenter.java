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
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="carpenter")
public class Carpenter {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long carpenterId;
   private String name;
   private String lastName;
   private String idNumber;

    @Column(nullable = true)
    private String rut;

   private String email;
   private String password;
   private String phone;

   @OneToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "workshop_id", referencedColumnName = "workshopId")
   private Workshop workshop;

   @OneToMany(mappedBy = "carpenter", cascade = CascadeType.ALL)
   private List<Furniture> furnitureList;

}
