package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Alert;
import com.blashape.backend_blashape.entitys.Carpenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByCarpenter(Carpenter carpenter);
}
