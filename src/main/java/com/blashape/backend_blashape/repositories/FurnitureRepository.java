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

    @Query("SELECT f FROM Furniture f WHERE f.carpenter.carpenterId = :carpenterId")
    List<Furniture> findFurnitureByCarpenterId(Long carpenterId);

    @Query("""
            SELECT f
            FROM Furniture f
            WHERE f.carpenter.carpenterId = :carpenterId
            AND f.creationDate BETWEEN :startDate AND :endDate
            ORDER BY f.creationDate DESC
        """)
    List<Furniture> findFurnitureByCarpenterIdAndCreationDateBetween(Long carpenterId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT f.type, COUNT(f)
            FROM Furniture f
            WHERE f.carpenter.carpenterId = :carpenterId
            GROUP BY f.type
            ORDER BY COUNT(f) DESC
            """)
    List<Object[]> countFurnitureByTypeForCarpenter(Long carpenterId);
    
    @Query("""
            SELECT CONCAT (f.customer.name, ' ', f.customer.lastName), f.customer.dni, COUNT(f)
            FROM Furniture f
            WHERE f.carpenter.carpenterId = :carpenterId
            AND f.customer IS NOT NULL
            GROUP BY f.customer.dni, f.customer.name, f.customer.lastName
            ORDER BY COUNT(f) DESC
            LIMIT :limit
            """)
    List<Object[]> findTopCustomersByFurnitureCountForCarpenter(Long carpenterId, int limit);

    @Query("""
            SELECT f.carpenter.carpenterId, CONCAT(f.carpenter.name, ' ', f.carpenter.lastName), COUNT(f)
            FROM Furniture f
            WHERE f.cutting IS NOT NULL
            GROUP BY f.carpenter.carpenterId, f.carpenter.name, f.carpenter.lastName
            ORDER BY COUNT(f) DESC
            LIMIT :limit
            """)
    List<Object[]> findTopCarpentersByCuttings(int limit); 
}
