package io.github.martinezdom.repairshop.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import io.github.martinezdom.repairshop.config.JwtAuthenticationFilter;
import io.github.martinezdom.repairshop.dtos.UserResponseDTO;
import io.github.martinezdom.repairshop.enums.Role;
import io.github.martinezdom.repairshop.services.JwtService;
import io.github.martinezdom.repairshop.services.UserService;

@WebMvcTest(controllers = UsersController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_Returns200() throws Exception {
        UserResponseDTO user = new UserResponseDTO();
        user.setId(1L);
        user.setUsername("mecanico1");
        user.setRole(Role.MECHANIC);

        Page<UserResponseDTO> page = new PageImpl<>(List.of(user));

        when(userService.getAllUsers(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/users?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("mecanico1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_Returns200() throws Exception {
        UserResponseDTO user = new UserResponseDTO();
        user.setId(1L);
        user.setUsername("admin1");
        user.setRole(Role.ADMIN);

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin1"));
    }
}
