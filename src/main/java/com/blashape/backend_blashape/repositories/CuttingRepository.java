package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Cutting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuttingRepository extends JpaRepository<Cutting, Long> {
    Optional<Cutting> findByFurnitureFurnitureId(Long furnitureId);
    boolean existsByFurnitureFurnitureId(Long furnitureId);

    @Query("SELECT c FROM Cutting c WHERE c.furniture.carpenter.carpenterId = :carpenterId")
    List<Cutting> findByCarpenterId(Long carpenterId);

}
