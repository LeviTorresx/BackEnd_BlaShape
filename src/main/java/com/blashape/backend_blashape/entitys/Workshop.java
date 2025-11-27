package com.blashape.backend_blashape.entitys;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name ="workshops")
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workshopId;
    private String address;

    @Column(nullable = true)
    private String nit;

    private String phone;
    private String name;

    @OneToOne
    @JoinColumn(name = "carpenter_id", referencedColumnName = "carpenterId")
    @JsonBackReference
    private Carpenter carpenter;

}
