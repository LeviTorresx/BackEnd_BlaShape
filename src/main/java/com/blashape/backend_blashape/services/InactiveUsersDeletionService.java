package com.blashape.backend_blashape.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InactiveUsersDeletionService {
    private final CustomerRepository customerRepository;
    private final CarpenterRepository carpenterRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteInactiveUsers() {
        customerRepository.deleteInactiveCustomersOlderThan30Days();
        carpenterRepository.deleteInactiveCarpentersOlderThan30Days();
    }

    @Scheduled(fixedRate = 10000) //Cada 10 segundos
    public void deleteInactiveUsersForTesting() {
        customerRepository.deleteInactiveCustomersOlderThan3Minutes();
        carpenterRepository.deleteInactiveCarpentersOlderThan3Minutes();
    }
}
