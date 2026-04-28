package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Customer;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE c.carpenter.carpenterId = :carpenterId AND c.isActive = true")
    List<Customer> findActiveCustomersByCarpenterId(@Param("carpenterId") Long carpenterId);

    boolean existsByDni(String dni);

    Optional<Customer> findByDni(String dni);

    boolean existsByDniAndCarpenter_CarpenterId(String dni, Long carpenterId);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM customer
            WHERE is_active = false
            AND NOW() >= deleted_at + INTERVAL '30 days'
            """, nativeQuery = true)
    void deleteInactiveCustomersOlderThan30Days();

    //Es para probar el método de eliminación por periodos, 3 minutos en este caso
    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM customer
            WHERE is_active = false
            AND NOW() >= deleted_at + INTERVAL '3 minutes'
            """, nativeQuery = true)
    void deleteInactiveCustomersOlderThan3Minutes();
}
