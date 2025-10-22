package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.CustomerDTO;
import com.blashape.backend_blashape.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api_BS/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final String mkey = "message";

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createCustomer(@RequestBody CustomerDTO dto){

        CustomerDTO customerDTO = customerService.createCustomer(dto);
        return ResponseEntity.ok(Map.of(mkey, "Cliente "+customerDTO.getName()+" creado exitosamente"));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CustomerDTO>> getAllByCarpenter(
            @CookieValue(name = "jwt", required = false) String token
    ) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CustomerDTO> customers = customerService.getCustomersByToken(token);
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<CustomerDTO> editCustomer(@PathVariable Long id, @RequestBody CustomerDTO dto){
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCustomer (@PathVariable Long id){
        return ResponseEntity.noContent().build();
    }


}
