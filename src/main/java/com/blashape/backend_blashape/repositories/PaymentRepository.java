package com.blashape.backend_blashape.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blashape.backend_blashape.entitys.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p WHERE p.carpenter.carpenterId = :carpenterId AND p.status = 'PAID' ORDER BY p.createdAt DESC")
    List<Payment> findPaidPaymentsByCarpenterId(@Param("carpenterId") Long carpenterId);
}
