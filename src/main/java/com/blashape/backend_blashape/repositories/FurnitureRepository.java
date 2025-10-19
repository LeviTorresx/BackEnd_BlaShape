package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Furniture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FurnitureRepository extends JpaRepository<Furniture, Long> {
    List<Furniture> findByCarpenter(Carpenter carpenter);

    @Query("SELECT f FROM Furniture f WHERE f.endDate BETWEEN :today AND :futureDate")
    List<Furniture> findByEndDateAndFutureDate(LocalDate today, LocalDate futureDate);
}
