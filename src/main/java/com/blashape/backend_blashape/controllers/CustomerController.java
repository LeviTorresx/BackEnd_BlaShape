package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.CustomerDTO;
import com.blashape.backend_blashape.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api_BS/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/create")
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO dto){
        return ResponseEntity.ok(customerService.createCustomer(dto));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<CustomerDTO> editCustomer(@PathVariable Long id, @RequestBody CustomerDTO dto){
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCustomer (@PathVariable Long id){
        return ResponseEntity.noContent().build();
    }


}
