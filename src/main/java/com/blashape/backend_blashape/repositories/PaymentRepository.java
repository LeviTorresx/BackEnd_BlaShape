package com.blashape.backend_blashape.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blashape.backend_blashape.entitys.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
}
