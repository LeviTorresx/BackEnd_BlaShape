package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Piece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceRepository extends JpaRepository<Piece, Long> {

    @Query("SELECT p FROM Piece p WHERE p.cutting.furniture.carpenter.carpenterId = :carpenterId")
    List<Piece> findByCarpenterId(Long carpenterId);
}
