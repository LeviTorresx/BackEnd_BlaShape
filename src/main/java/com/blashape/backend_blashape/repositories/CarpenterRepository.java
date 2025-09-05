package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Carpenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarpenterRepository extends JpaRepository<Carpenter, Long> {
    boolean existsByEmail(String email);
    boolean existsByIdNumber(String idNumber);
}
