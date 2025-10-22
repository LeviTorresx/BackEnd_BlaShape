package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByCarpenter_CarpenterId(Long carpenterId);

}
