package com.blashape.backend_blashape.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blashape.backend_blashape.entitys.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
}
