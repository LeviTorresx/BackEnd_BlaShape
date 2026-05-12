package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Pqrs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PqrsRepository extends JpaRepository<Pqrs, Long> {

    @Query("SELECT p FROM Pqrs p WHERE p.pqrsId = :id AND p.deleted = false")
    Optional<Pqrs> findActiveById(@Param("id") Long id);

    @Query("SELECT p FROM Pqrs p WHERE p.trackingCode = :code AND p.deleted = false")
    Optional<Pqrs> findActiveByTrackingCode(@Param("code") String code);

    @Query("SELECT p FROM Pqrs p WHERE p.carpenter.carpenterId = :carpenterId AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Pqrs> findActiveByCarpenterId(@Param("carpenterId") Long carpenterId);

    @Query("SELECT p FROM Pqrs p WHERE p.customer.customerId = :customerId AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Pqrs> findActiveByCustomerId(@Param("customerId") Long customerId);

    // Para auto-link cuando se cree un Customer con email que ya tenía PQRS de invitado
    @Query("SELECT p FROM Pqrs p " +
            "WHERE p.customer IS NULL " +
            "AND LOWER(p.guestEmail) = LOWER(:email) " +
            "AND p.carpenter.carpenterId = :carpenterId " +
            "AND p.deleted = false")
    List<Pqrs> findOrphanByGuestEmail(@Param("email") String email,
                                      @Param("carpenterId") Long carpenterId);
}