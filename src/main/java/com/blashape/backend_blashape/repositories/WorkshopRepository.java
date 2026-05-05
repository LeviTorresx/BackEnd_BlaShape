package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Workshop;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    boolean existsByNit(String nit);

    @Query("SELECT w FROM Workshop w " +
            "WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND w.carpenter IS NOT NULL " +
            "ORDER BY w.name ASC")
    List<Workshop> searchByNameContaining(@Param("query") String query, Pageable pageable);
}
