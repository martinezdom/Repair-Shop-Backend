package io.github.martinezdom.repairshop.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.martinezdom.repairshop.config.JwtAuthenticationFilter;
import io.github.martinezdom.repairshop.dtos.RepairCreateDTO;
import io.github.martinezdom.repairshop.dtos.RepairResponseDTO;
import io.github.martinezdom.repairshop.dtos.RepairUpdateDTO;
import io.github.martinezdom.repairshop.enums.RepairStatus;
import io.github.martinezdom.repairshop.services.JwtService;
import io.github.martinezdom.repairshop.services.RepairService;

@WebMvcTest(controllers = RepairController.class)
@AutoConfigureMockMvc(addFilters = false)
class RepairControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepairService repairService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Test
    @WithMockUser(roles = "MECHANIC")
    void createRepair_Returns201() throws Exception {
        RepairCreateDTO requestDto = new RepairCreateDTO();
        requestDto.setVehicleId(1L);
        requestDto.setMechanicId(2L);
        requestDto.setDescription("Frenos");

        RepairResponseDTO responseDto = new RepairResponseDTO();
        responseDto.setId(1L);
        responseDto.setDescription("Frenos");
        responseDto.setStatus(RepairStatus.PENDIENTE);

        when(repairService.createRepair(any(RepairCreateDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/repairs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Frenos"));
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void getAllRepairs_Returns200() throws Exception {
        RepairResponseDTO repair = new RepairResponseDTO();
        repair.setId(1L);
        repair.setDescription("Frenos");

        Page<RepairResponseDTO> page = new PageImpl<>(List.of(repair));

        when(repairService.getAllRepairs(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/repairs?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Frenos"));
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void getRepairById_Returns200() throws Exception {
        RepairResponseDTO repair = new RepairResponseDTO();
        repair.setId(1L);
        repair.setDescription("Frenos");

        when(repairService.getRepairById(1L)).thenReturn(repair);

        mockMvc.perform(get("/api/repairs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Frenos"));
    }

    @Test
    void updateRepair_Returns200() throws Exception {
        RepairResponseDTO responseDto = new RepairResponseDTO();
        responseDto.setId(1L);
        responseDto.setStatus(RepairStatus.TERMINADO);

        when(repairService.updateRepair(eq(1L), any(RepairUpdateDTO.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/repairs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cost\":null,\"status\":\"terminado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", org.hamcrest.Matchers.equalToIgnoringCase("TERMINADO")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRepair_Returns204() throws Exception {
        doNothing().when(repairService).deleteRepair(1L);

        mockMvc.perform(delete("/api/repairs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void getRepairsByStatus_Returns200() throws Exception {
        RepairResponseDTO repair = new RepairResponseDTO();
        repair.setStatus(RepairStatus.PENDIENTE);

        Page<RepairResponseDTO> page = new PageImpl<>(List.of(repair));

        when(repairService.getRepairsByStatus(anyString(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/repairs/status/pendiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status", org.hamcrest.Matchers.equalToIgnoringCase("PENDIENTE")));
    }

    @Test
    void getMyRepairs_Returns200() throws Exception {
        io.github.martinezdom.repairshop.entities.User mockUser = new io.github.martinezdom.repairshop.entities.User();
        mockUser.setId(1L);
        mockUser.setRole(io.github.martinezdom.repairshop.enums.Role.MECHANIC);
        mockUser.setPasswordHash("password");
        mockUser.setEmail("test@test.com");

        RepairResponseDTO repair = new RepairResponseDTO();
        repair.setMechanicName("mecanico1");

        Page<RepairResponseDTO> page = new PageImpl<>(List.of(repair));

        when(repairService.getMyRepairs(any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/repairs/my-repairs")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].mechanicName").value("mecanico1"));
    }
}
