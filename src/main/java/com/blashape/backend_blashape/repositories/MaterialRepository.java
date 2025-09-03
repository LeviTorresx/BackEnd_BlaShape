package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository  extends JpaRepository<Material, Long> {
}
