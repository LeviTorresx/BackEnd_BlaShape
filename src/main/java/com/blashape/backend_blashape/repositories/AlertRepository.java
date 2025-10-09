package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
}
