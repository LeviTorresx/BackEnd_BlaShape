package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Piece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PieceRepository extends JpaRepository<Piece, Long> {
}
