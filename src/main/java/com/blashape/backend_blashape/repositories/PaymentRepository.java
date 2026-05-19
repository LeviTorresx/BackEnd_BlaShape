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

    @Query("""
            SELECT COUNT(p) 
            FROM Payment p
            WHERE p.paymentType = 'ONE_TIME_PRODUCT'
            AND p.status = 'PAID'
            """)
    Long countPaidOneTimeProductPayments();

    @Query("""
            SELECT COUNT(p) 
            FROM Payment p
            WHERE p.paymentType = 'SUBSCRIPTION'
            AND p.status = 'PAID'
            """)
    Long countPaidSubscriptionPayments();

    @Query("""
            SELECT SUM(p.amount)
            FROM Payment p
            WHERE p.status = 'PAID'
            AND p.createdAt
            BETWEEN :startDate 
            AND :endDate
            """)
    Long sumPaidPaymentsBetweenDates(@Param("startDate") java.time.Instant startDate, @Param("endDate") java.time.Instant endDate);

    @Query("""
            SELECT p.carpenter.carpenterId, CONCAT(p.carpenter.name, ' ', p.carpenter.lastName), COUNT(p)
            FROM Payment p
            WHERE p.status = 'PAID'
            GROUP BY p.carpenter.carpenterId, p.carpenter.name, p.carpenter.lastName
            ORDER BY COUNT(p) DESC
            LIMIT :limit
            """)
    List<Object[]> findTopCarpentersByPaidPayments(int limit);

    @Query("""
            SELECT p.carpenter.carpenterId, CONCAT(p.carpenter.name, ' ', p.carpenter.lastName), SUM(p.amount)
            FROM Payment p
            WHERE p.status = 'PAID'
            GROUP BY p.carpenter.carpenterId, p.carpenter.name, p.carpenter.lastName
            ORDER BY SUM(p.amount) DESC
            LIMIT :limit
            """)
    List<Object[]> findTopCarpentersByAmount(int limit);

    
}
