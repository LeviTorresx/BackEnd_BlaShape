package com.blashape.backend_blashape.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blashape.backend_blashape.entitys.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

}
