package io.github.martinezdom.repairshop.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import io.github.martinezdom.repairshop.config.JwtAuthenticationFilter;
import io.github.martinezdom.repairshop.dtos.DashboardResponseDTO;
import io.github.martinezdom.repairshop.services.JwtService;
import io.github.martinezdom.repairshop.services.StatsService;

@WebMvcTest(controllers = StatsController.class)
@AutoConfigureMockMvc(addFilters = false) 
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatsService statsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getStats_Returns200_ForAdmin() throws Exception {
        DashboardResponseDTO dashboardData = new DashboardResponseDTO();
        dashboardData.setPendingCars(10L);
        dashboardData.setTotalRevenue(new BigDecimal("5000.00"));

        when(statsService.getDashboardStats()).thenReturn(dashboardData);

        mockMvc.perform(get("/api/stats/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCars").value(10))
                .andExpect(jsonPath("$.totalRevenue").value(5000.00));
    }
}
