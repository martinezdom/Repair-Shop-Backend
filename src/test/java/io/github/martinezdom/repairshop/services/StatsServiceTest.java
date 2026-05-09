package io.github.martinezdom.repairshop.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.martinezdom.repairshop.dtos.DashboardResponseDTO;
import io.github.martinezdom.repairshop.enums.RepairStatus;
import io.github.martinezdom.repairshop.repositories.RepairRepository;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private RepairRepository repairRepository;

    @InjectMocks
    private StatsService statsService;

    @Test
    void getDashboardStats_ReturnsData() {
        when(repairRepository.countByStatus(RepairStatus.PENDIENTE)).thenReturn(5L);
        when(repairRepository.sumCostByStatus(RepairStatus.TERMINADO)).thenReturn(new BigDecimal("1500.50"));

        DashboardResponseDTO result = statsService.getDashboardStats();

        assertEquals(5L, result.getPendingCars());
        assertEquals(new BigDecimal("1500.50"), result.getTotalRevenue());
    }

    @Test
    void getDashboardStats_HandlesNullRevenue() {
        when(repairRepository.countByStatus(RepairStatus.PENDIENTE)).thenReturn(2L);
        when(repairRepository.sumCostByStatus(RepairStatus.TERMINADO)).thenReturn(null);

        DashboardResponseDTO result = statsService.getDashboardStats();

        assertEquals(2L, result.getPendingCars());
        assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
    }
}
