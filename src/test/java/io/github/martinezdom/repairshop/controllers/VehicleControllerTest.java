package io.github.martinezdom.repairshop.controllers;

import static org.mockito.ArgumentMatchers.any;
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
import io.github.martinezdom.repairshop.dtos.VehicleCreateDTO;
import io.github.martinezdom.repairshop.dtos.VehicleResponseDTO;
import io.github.martinezdom.repairshop.dtos.VehicleUpdateDTO;
import io.github.martinezdom.repairshop.services.JwtService;
import io.github.martinezdom.repairshop.services.VehicleService;

@WebMvcTest(controllers = VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleService vehicleService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Test
    @WithMockUser(roles = "MECHANIC")
    void createVehicle_Returns201() throws Exception {
        VehicleCreateDTO requestDto = new VehicleCreateDTO();
        requestDto.setBrand("Seat");
        requestDto.setModel("Leon");
        requestDto.setLicensePlate("1234ABC");
        requestDto.setYear(2020);
        requestDto.setCustomerId(1L);

        VehicleResponseDTO responseDto = new VehicleResponseDTO();
        responseDto.setId(1L);
        responseDto.setLicensePlate("1234ABC");

        when(vehicleService.createVehicle(any(VehicleCreateDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licensePlate").value("1234ABC"));
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void getVehicles_Returns200() throws Exception {
        VehicleResponseDTO vehicle = new VehicleResponseDTO();
        vehicle.setId(1L);
        vehicle.setLicensePlate("1234ABC");

        Page<VehicleResponseDTO> page = new PageImpl<>(List.of(vehicle));

        when(vehicleService.getAllVehicles(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/vehicles?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].licensePlate").value("1234ABC"));
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void getVehicleById_Returns200() throws Exception {
        VehicleResponseDTO vehicle = new VehicleResponseDTO();
        vehicle.setId(1L);
        vehicle.setLicensePlate("1234ABC");

        when(vehicleService.getVehicleById(1L)).thenReturn(vehicle);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("1234ABC"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteVehicle_Returns204() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(1L);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void updateVehicle_Returns200() throws Exception {
        VehicleUpdateDTO requestDto = new VehicleUpdateDTO();
        requestDto.setBrand("Audi");
        requestDto.setModel("A3");
        requestDto.setYear(2021);

        VehicleResponseDTO responseDto = new VehicleResponseDTO();
        responseDto.setId(1L);
        responseDto.setBrand("Audi");

        when(vehicleService.updateVehicle(eq(1L), any(VehicleUpdateDTO.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Audi"));
    }
}
