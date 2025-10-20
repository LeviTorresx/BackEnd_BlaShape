package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Alert;
import com.blashape.backend_blashape.entitys.Carpenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByCarpenter(Carpenter carpenter);
    boolean existsByCarpenterAndDateAndMessage(Carpenter carpenter, LocalDate date, String message);
}
