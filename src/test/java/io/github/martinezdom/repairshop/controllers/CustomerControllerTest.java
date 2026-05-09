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
import io.github.martinezdom.repairshop.dtos.CustomerCreateDTO;
import io.github.martinezdom.repairshop.dtos.CustomerResponseDTO;
import io.github.martinezdom.repairshop.dtos.CustomerUpdateDTO;
import io.github.martinezdom.repairshop.services.CustomerService;
import io.github.martinezdom.repairshop.services.JwtService;

@WebMvcTest(controllers = CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Test
    @WithMockUser(roles = "MECHANIC")
    void createCustomer_Returns201() throws Exception {
        CustomerCreateDTO requestDto = new CustomerCreateDTO();
        requestDto.setFirstName("Pedro");
        requestDto.setLastName("Gomez");
        requestDto.setEmail("pedro@gmail.com");
        requestDto.setPhone("123123123");

        CustomerResponseDTO responseDto = new CustomerResponseDTO();
        responseDto.setId(1L);
        responseDto.setFirstName("Pedro");

        when(customerService.createCustomer(any(CustomerCreateDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void getCustomers_Returns200() throws Exception {
        CustomerResponseDTO customer = new CustomerResponseDTO();
        customer.setId(1L);
        customer.setFirstName("Pedro");

        Page<CustomerResponseDTO> page = new PageImpl<>(List.of(customer));

        when(customerService.getAllCustomers(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/customers?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Pedro"));
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void getCustomerById_Returns200() throws Exception {
        CustomerResponseDTO customer = new CustomerResponseDTO();
        customer.setId(1L);
        customer.setFirstName("Pedro");

        when(customerService.getCustomerById(1L)).thenReturn(customer);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Pedro"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCustomer_Returns204() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MECHANIC")
    void updateCustomer_Returns200() throws Exception {
        CustomerUpdateDTO requestDto = new CustomerUpdateDTO();
        requestDto.setEmail("nuevo@gmail.com");
        requestDto.setPhone("999999999");

        CustomerResponseDTO responseDto = new CustomerResponseDTO();
        responseDto.setId(1L);
        responseDto.setEmail("nuevo@gmail.com");

        when(customerService.updateCustomer(eq(1L), any(CustomerUpdateDTO.class))).thenReturn(responseDto);

        mockMvc.perform(put("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo@gmail.com"));
    }
}
