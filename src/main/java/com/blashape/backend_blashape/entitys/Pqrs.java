package com.blashape.backend_blashape.entitys;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pqrs", indexes = {
        @Index(name = "idx_pqrs_tracking_code", columnList = "trackingCode", unique = true),
        @Index(name = "idx_pqrs_guest_email", columnList = "guestEmail")
})
public class Pqrs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    // ===== Identidad: opción A — usuario registrado =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId", nullable = true)
    private Customer customer;

    // ===== Identidad: opción B — invitado (datos snapshot al momento de radicar) =====
    @Column(length = 100)
    private String guestName;

    @Column(length = 100)
    private String guestLastName;

    @Column(length = 150)
    private String guestEmail;

    @Column(length = 30)
    private String guestPhone;

    // ===== Destinatario =====
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "carpenter_id", referencedColumnName = "carpenterId", nullable = false)
    private Carpenter carpenter;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PqrsStatus.PENDIENTE;
        }
    }
}