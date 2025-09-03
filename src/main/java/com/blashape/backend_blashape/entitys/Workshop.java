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
@Table(name ="workshop")
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workshopId;
    private String address;

    @Column(nullable = true)
    private String nit;

    private String phone;
    private String name;

    @OneToOne(mappedBy = "workshop", cascade = CascadeType.ALL)
    private Carpenter carpenter;

}
