package com.blashape.backend_blashape.entitys;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="alerts")

public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;

    private String message;
    private LocalDate date;
    private LocalTime time;

    @Enumerated(EnumType.STRING)
    private AlertState state = AlertState.ACTIVE;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @ManyToOne
    @JoinColumn(name = "carpenter_id", referencedColumnName = "carpenterId")
    @JsonBackReference(value = "carpenter-alert")
    private Carpenter carpenter;
}
