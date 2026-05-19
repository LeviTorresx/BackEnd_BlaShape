package com.blashape.backend_blashape.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.DTOs.FurnitureTypeCount;
import com.blashape.backend_blashape.DTOs.MostUsedMaterial;
import com.blashape.backend_blashape.DTOs.SubscriptionPlan;
import com.blashape.backend_blashape.DTOs.TopCarpenter;
import com.blashape.backend_blashape.DTOs.TopCarpenterAmount;
import com.blashape.backend_blashape.DTOs.TopCustomer;
import com.blashape.backend_blashape.entitys.FurnitureType;
import com.blashape.backend_blashape.mapper.FurnitureMapper;
import com.blashape.backend_blashape.repositories.CuttingRepository;
import com.blashape.backend_blashape.repositories.FurnitureRepository;
import com.blashape.backend_blashape.repositories.PaymentRepository;
import com.blashape.backend_blashape.repositories.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final FurnitureRepository furnitureRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final FurnitureMapper furnitureMapper;
    private final CuttingRepository cuttingRepository;

    public List<FurnitureDTO> furnituresByDate(Long carpenterId, LocalDate startDate, LocalDate endDate) {
        return furnitureRepository.findFurnitureByCarpenterIdAndCreationDateBetween(carpenterId, startDate, endDate)
                .stream()
                .map(furnitureMapper::toDTO)
                .toList();
    }

    public List<FurnitureTypeCount> furnitureByType(Long carpenterId) {
        return furnitureRepository.countFurnitureByTypeForCarpenter(carpenterId)
                .stream()
                .map(result -> new FurnitureTypeCount((FurnitureType) result[0], ((Number) result[1]).longValue()))
                .toList();
    }

    public List<TopCustomer> topCustomersForCarpenter(Long carpenterId, int limit) {
        return furnitureRepository.findTopCustomersByFurnitureCountForCarpenter(carpenterId, limit)
                .stream()
                .map(result -> new TopCustomer((String) result[0], (String) result[1], ((Number) result[2]).longValue()))
                .toList();
    }

    public List<TopCarpenter> topCarpentersByCuttings(int limit) {
        return furnitureRepository.findTopCarpentersByCuttings(limit)
                .stream()
                .map(result -> new TopCarpenter(((Number) result[0]).longValue(), (String) result[1], ((Number) result[2]).longValue()))
                .toList();
    }

    public List<MostUsedMaterial> mostUsedMaterialsForCarpenter(Long carpenterId) {
        return cuttingRepository.findMostUsedMaterialsByCarpenterId(carpenterId)
                .stream()
                .map(result -> new MostUsedMaterial((String) result[0], ((Number) result[1]).longValue()))
                .toList();
    }

    public Long oneTimeProductPaymentsCount() {
        return paymentRepository.countPaidOneTimeProductPayments();
    }

    public Long subscriptionPaymentsCount() {
        return paymentRepository.countPaidSubscriptionPayments();
    }

    public Long totalAmountBetweenDates(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.sumPaidPaymentsBetweenDates(startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC), endDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
    }

    public List<TopCarpenter> topCarpentersByPaidPayments(int limit) {
        return paymentRepository.findTopCarpentersByPaidPayments(limit)
                .stream()
                .map(result -> new TopCarpenter(((Number) result[0]).longValue(), (String) result[1], ((Number) result[2]).longValue()))
                .toList();
    }

    public List<TopCarpenterAmount> topCarpentersByAmount(int limit) {
        return paymentRepository.findTopCarpentersByAmount(limit)
                .stream()
                .map(result -> new TopCarpenterAmount(((Number) result[0]).longValue(), (String) result[1], ((Number) result[2]).longValue()))
                .toList();
    }

    public Long activeSubscriptionsCount() {
        return subscriptionRepository.countActiveSubscriptions();
    }

    public List<SubscriptionPlan> subscriptionsByPlan() {
        return subscriptionRepository.subscriptionsByPlan()
                .stream()
                .map(result -> new SubscriptionPlan((String) result[0], ((Number) result[1]).longValue()))
                .toList();
    }
}
