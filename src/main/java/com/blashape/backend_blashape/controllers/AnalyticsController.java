package com.blashape.backend_blashape.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blashape.backend_blashape.DTOs.FurnitureDTO;
import com.blashape.backend_blashape.DTOs.FurnitureTypeCount;
import com.blashape.backend_blashape.DTOs.MostUsedMaterial;
import com.blashape.backend_blashape.DTOs.SubscriptionPlan;
import com.blashape.backend_blashape.DTOs.TopCarpenter;
import com.blashape.backend_blashape.DTOs.TopCarpenterAmount;
import com.blashape.backend_blashape.DTOs.TopCustomer;
import com.blashape.backend_blashape.services.AnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api_BS/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    //Muebles creados por rango de fechas (si es para una sola fecha se pone la misma en ambos parámetros)
    @GetMapping("/furnitures-by-date")
    public ResponseEntity<List<FurnitureDTO>> getFurnituresByDate(@RequestParam Long carpenterId,
                                                                  @RequestParam String startDate,
                                                                  @RequestParam String endDate) {
        return ResponseEntity.ok(analyticsService.furnituresByDate(carpenterId, LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    //Obtener los muebles por cada tipo
    @GetMapping("/furnitures-by-type")
    public ResponseEntity<List<FurnitureTypeCount>> getFurnituresByType(@RequestParam Long carpenterId) {
        return ResponseEntity.ok(analyticsService.furnitureByType(carpenterId));
    }

    //Obtener los clientes más frecuentes (que más muebles han comprado) para un carpintero (como no hay relacion muchos a muchos, un cliente es igual por dni)
    @GetMapping("/top-customers")
    public ResponseEntity<List<TopCustomer>> getTopCustomers(@RequestParam Long carpenterId,
                                                            @RequestParam int limit) {
        return ResponseEntity.ok(analyticsService.topCustomersForCarpenter(carpenterId, limit));
    }

    //Obtener los carpinteros con más cortes realizados
    @GetMapping("/top-carpenters-by-cuttings")
    public ResponseEntity<List<TopCarpenter>> getTopCarpentersByCuttings(@RequestParam int limit) {
        return ResponseEntity.ok(analyticsService.topCarpentersByCuttings(limit));
    }

    //Obtener los materiales más usados por un carpintero
    @GetMapping("/most-used-materials")
    public ResponseEntity<List<MostUsedMaterial>> getMostUsedMaterials(@RequestParam Long carpenterId) {
        return ResponseEntity.ok(analyticsService.mostUsedMaterialsForCarpenter(carpenterId));
    }

    //Obtener la cantidad de productos de un solo pago que se han vendido
    @GetMapping("/one-time-products-paid-count")
    public ResponseEntity<Long> getOneTimeProductsCount() {
        return ResponseEntity.ok(analyticsService.oneTimeProductPaymentsCount());
    }

    //Obtener la cantidad de suscripciones que se han pagado
    @GetMapping("/subscriptions-paid-count")
    public ResponseEntity<Long> getSubscriptionsCount() {
        return ResponseEntity.ok(analyticsService.subscriptionPaymentsCount());
    }

    //Total vendido entre dos fechas
    @GetMapping("/total-selled-between-dates")
    public ResponseEntity<Long> getTotalAmountBetweenDates(@RequestParam String startDate, @RequestParam String endDate) {
        return ResponseEntity.ok(analyticsService.totalAmountBetweenDates(LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    //Obtener los carpinteros con más pagos realizados
    @GetMapping("/top-carpenters-by-paid-payments")
    public ResponseEntity<List<TopCarpenter>> getTopCarpentersByPaidPayments(@RequestParam int limit) {
        return ResponseEntity.ok(analyticsService.topCarpentersByPaidPayments(limit));
    }

    //Obtener los carpinteros con más dinero generado por pagos realizados
    @GetMapping("/top-carpenters-by-amount")
    public ResponseEntity<List<TopCarpenterAmount>> getTopCarpentersByAmount(@RequestParam int limit) {
        return ResponseEntity.ok(analyticsService.topCarpentersByAmount(limit));
    }

    //Obtener la cantidad de suscripciones activas
    @GetMapping("/get-active-subscriptions-count")
    public ResponseEntity<Long> getActiveSubscriptionsCount() {
        return ResponseEntity.ok(analyticsService.activeSubscriptionsCount());
    }

    //Obtener la cantidad de suscripciones activas por plan
    @GetMapping("/subscriptions-by-plan")
    public ResponseEntity<List<SubscriptionPlan>> getSubscriptionsByPlan() {
        return ResponseEntity.ok(analyticsService.subscriptionsByPlan());
    }
}
