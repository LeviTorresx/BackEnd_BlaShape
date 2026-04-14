package com.blashape.backend_blashape.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blashape.backend_blashape.DTOs.ActiveSubscription;
import com.blashape.backend_blashape.DTOs.PaymentDTO;
import com.blashape.backend_blashape.DTOs.PlanDTO;
import com.blashape.backend_blashape.DTOs.ProductDTO;
import com.blashape.backend_blashape.services.MonetizationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api_BS/monetization")
@RequiredArgsConstructor
public class MonetizationController {
    private final MonetizationService monetizationService;

    @PostMapping("/create-plan")
    public ResponseEntity<PlanDTO> createPlan(@RequestBody PlanDTO dto) {
        return ResponseEntity.ok(monetizationService.createPlan(dto));
    }

    @PostMapping("/create-product")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO dto) {
        return ResponseEntity.ok(monetizationService.createProduct(dto));
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanDTO>> getPlans() {
        return ResponseEntity.ok(monetizationService.getAllPlans());
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getProducts() {
        return ResponseEntity.ok(monetizationService.getAllProducts());
    }

    @GetMapping("/plan/{id}")
    public ResponseEntity<PlanDTO> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(monetizationService.getPlanById(id));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(monetizationService.getProductById(id));
    }

    @PutMapping("/edit-plan/{id}")
    public ResponseEntity<PlanDTO> updatePlan(@PathVariable Long id, @RequestBody PlanDTO dto) {
        return ResponseEntity.ok(monetizationService.updatePlan(id, dto));
    }

    @PutMapping("/edit-product/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(monetizationService.updateProduct(id, dto));
    }

    @DeleteMapping("/delete-plan/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        monetizationService.deletePlan(id);
        return ResponseEntity.ok("Plan eliminado correctamente");
    }

    @DeleteMapping("/delete-product/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        monetizationService.deleteProduct(id);
        return ResponseEntity.ok("Producto eliminado correctamente");
    }

    @GetMapping("/active-subscription/{carpenterId}")
    public ResponseEntity<ActiveSubscription> getActiveSubscriptionByCarpenterId(@PathVariable Long carpenterId) {
        return ResponseEntity.ok(monetizationService.getActivePlanByCarpenterId(carpenterId));
    }

    @GetMapping("/paid-payments/{carpenterId}")
    public ResponseEntity<List<PaymentDTO>> getPaidPaymentsByCarpenterId(@PathVariable Long carpenterId) {
        return ResponseEntity.ok(monetizationService.getPaidPaymentsByCarpenterId(carpenterId));
    }
}
