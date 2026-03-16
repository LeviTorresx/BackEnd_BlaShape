package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Carpenter;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarpenterRepository extends JpaRepository<Carpenter, Long> {
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
    Optional<Carpenter> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM carpenter
            WHERE is_active = false
            AND NOW() >= deleted_at + INTERVAL '30 days'
            """, nativeQuery = true)
    void deleteInactiveCarpentersOlderThan30Days();

    //Es para probar el método de eliminación por periodos, 3 minutos en este caso
    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM carpenter
            WHERE is_active = false
            AND NOW() >= deleted_at + INTERVAL '3 minutes'
            """, nativeQuery = true)
    void deleteInactiveCarpentersOlderThan3Minutes();
}
