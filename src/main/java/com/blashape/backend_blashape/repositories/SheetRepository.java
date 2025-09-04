package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Sheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SheetRepository extends JpaRepository<Sheet, Long> {
}
