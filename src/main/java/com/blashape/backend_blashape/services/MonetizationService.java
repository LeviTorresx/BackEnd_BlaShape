package com.blashape.backend_blashape.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.blashape.backend_blashape.DTOs.PlanDTO;
import com.blashape.backend_blashape.DTOs.ProductDTO;
import com.blashape.backend_blashape.entitys.Plan;
import com.blashape.backend_blashape.entitys.Product;
import com.blashape.backend_blashape.mapper.PlanMapper;
import com.blashape.backend_blashape.mapper.ProductMapper;
import com.blashape.backend_blashape.repositories.PlanRepository;
import com.blashape.backend_blashape.repositories.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonetizationService {
    private final PlanRepository planRepository;
    private final ProductRepository productRepository;
    private final PlanMapper planMapper;
    private final ProductMapper productMapper;
    private static final String PLAN_NO_ENCONTRADO = "Plan no encontrado con ID: ";
    private static final String PRODUCTO_NO_ENCONTRADO = "Producto no encontrado con ID: ";

    public PlanDTO getPlanById(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PLAN_NO_ENCONTRADO + id));

        return planMapper.toDTO(plan);
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PRODUCTO_NO_ENCONTRADO + id));

        return productMapper.toDTO(product);
    }

    public List<PlanDTO> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(planMapper::toDTO)
                .toList();
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toDTO)
                .toList();
    }

    public PlanDTO createPlan(PlanDTO dto) {
        Plan plan = planMapper.toEntity(dto);
        Plan saved = planRepository.save(plan);
        return planMapper.toDTO(saved);
    }

    public ProductDTO createProduct(ProductDTO dto) {
        Product product = productMapper.toEntity(dto);
        Product saved = productRepository.save(product);
        return productMapper.toDTO(saved);
    }

    public PlanDTO updatePlan(Long id, PlanDTO dto) {
        Plan existing = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PLAN_NO_ENCONTRADO + id));

        if (dto.getPlanName() != null && !dto.getPlanName().isBlank()) {
            existing.setPlanName(dto.getPlanName());
        }
        if (dto.getPrice() != null) {
            existing.setPrice(dto.getPrice());
        }
        if (dto.getCurrency() != null && !dto.getCurrency().isBlank()) {
            existing.setCurrency(dto.getCurrency());
        }
        if (dto.getCuttingLimit() != null) {
            existing.setCuttingLimit(dto.getCuttingLimit());
        }
        if (dto.getSvg() != null) {
            existing.setSvg(dto.getSvg());
        }
        if (dto.getLimitedSvg() != null) {
            existing.setLimitedSvg(dto.getLimitedSvg());
        }
        if (dto.getPdf() != null) {
            existing.setPdf(dto.getPdf());
        }
        if (dto.getLimitedRecord() != null) {
            existing.setLimitedRecord(dto.getLimitedRecord());
        }
        if (dto.getDuration() != null) {
            existing.setDuration(dto.getDuration());
        }
        if (dto.getMeaningPieces() != null) {
            existing.setMeaningPieces(dto.getMeaningPieces());
        }
        if (dto.getAnalyticsModule() != null) {
            existing.setAnalyticsModule(dto.getAnalyticsModule());
        }
        if (dto.getBusinessLicence() != null) {
            existing.setBusinessLicence(dto.getBusinessLicence());
        }

        Plan updated = planRepository.save(existing);
        return planMapper.toDTO(updated);
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(PRODUCTO_NO_ENCONTRADO + id));

        if (dto.getProductName() != null && !dto.getProductName().isBlank()) {
            existing.setProductName(dto.getProductName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            existing.setPrice(dto.getPrice());
        }
        if (dto.getCurrency() != null && !dto.getCurrency().isBlank()) {
            existing.setCurrency(dto.getCurrency());
        }

        Product updated = productRepository.save(existing);
        return productMapper.toDTO(updated);
    }

    public void deletePlan(Long id) {
        planRepository.deleteById(id);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
