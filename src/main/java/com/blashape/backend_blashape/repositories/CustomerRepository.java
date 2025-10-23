package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE c.carpenter.carpenterId = :carpenterId AND c.deleted = false")
    List<Customer> findActiveCustomersByCarpenterId(@Param("carpenterId") Long carpenterId);

}
