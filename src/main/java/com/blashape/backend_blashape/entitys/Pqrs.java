package com.blashape.backend_blashape.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "pqrs", indexes = {
        @Index(name = "idx_pqrs_tracking_code", columnList = "trackingCode", unique = true),
        @Index(name = "idx_pqrs_guest_email", columnList = "guestEmail"),
        @Index(name = "idx_pqrs_scope", columnList = "scope")
})
public class Pqrs {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pqrsId;

    @Column(nullable = false, length = 150)
    private String subject;
    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PqrsType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PqrsStatus status = PqrsStatus.PENDIENTE;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ColumnDefault("'WORKSHOP'")
    private PqrsScope scope = PqrsScope.WORKSHOP;

    @Column(length = 2000)
    private String response;
    @Column(nullable = false, updatable = false, unique = true, length = 50)
    private String trackingCode;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    @Column(nullable = false)
    private boolean deleted = false;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    private Customer customer;

    @Column(length = 100)
    private String guestName;
    @Column(length = 100)
    private String guestLastName;
    @Column(length = 150)
    private String guestEmail;
    @Column(length = 30)
    private String guestPhone;

    /** Carpintero asignado: dueño del taller (WORKSHOP) o usuario PQRS_RECEIVER (GENERAL). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carpenter_id", referencedColumnName = "carpenterId")
    private Carpenter carpenter;

    /** Taller destinatario, sólo cuando scope = WORKSHOP. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id", referencedColumnName = "workshopId")
    private Workshop workshop;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = PqrsStatus.PENDIENTE;
        if (this.scope == null)  this.scope  = PqrsScope.WORKSHOP;
    }
}