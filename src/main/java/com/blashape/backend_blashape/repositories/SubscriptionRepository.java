package com.blashape.backend_blashape.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.blashape.backend_blashape.entitys.AppSubscription;
import com.blashape.backend_blashape.entitys.SubscriptionStatus;

@Repository
public interface SubscriptionRepository extends JpaRepository<AppSubscription, Long> {
    Boolean existsByCarpenter_CarpenterIdAndStatus(Long carpenterId, SubscriptionStatus status);

    Optional<AppSubscription> findByCarpenter_CarpenterIdAndStatus(Long carpenterId, SubscriptionStatus status);

    List<AppSubscription> findByStatusInAndEndDateBefore(List<SubscriptionStatus> statuses, Instant endDate);

    @Query("SELECT s.plan.planId FROM AppSubscription s WHERE s.carpenter.carpenterId = :carpenterId AND s.status = :activeStatus")
    Long getPlanIdForActiveSubscription(Long carpenterId, SubscriptionStatus activeStatus);

    @Query("""
            SELECT COUNT(s)
            FROM AppSubscription s
            WHERE s.status = 'ACTIVE'
            """)
    Long countActiveSubscriptions();

    @Query("""
            SELECT s.plan.planName, COUNT(s)
            FROM AppSubscription s
            GROUP BY s.plan.planName
            ORDER BY COUNT(s) DESC
            """)
    List<Object[]> subscriptionsByPlan();
}
