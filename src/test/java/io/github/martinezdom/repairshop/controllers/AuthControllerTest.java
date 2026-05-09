package io.github.martinezdom.repairshop.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.martinezdom.repairshop.config.JwtAuthenticationFilter;
import io.github.martinezdom.repairshop.dtos.TokenResponseDTO;
import io.github.martinezdom.repairshop.dtos.UserLoginDTO;
import io.github.martinezdom.repairshop.dtos.UserRegisterDTO;
import io.github.martinezdom.repairshop.dtos.UserResponseDTO;
import io.github.martinezdom.repairshop.enums.Role;
import io.github.martinezdom.repairshop.services.JwtService;
import io.github.martinezdom.repairshop.services.UserService;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Test
    void register_Returns201_WhenValid() throws Exception {
        UserRegisterDTO requestDto = new UserRegisterDTO();
        requestDto.setUsername("nuevoUser");
        requestDto.setEmail("test@test.com");
        requestDto.setPassword("12345678");

        UserResponseDTO responseDto = new UserResponseDTO();
        responseDto.setId(1L);
        responseDto.setUsername("nuevoUser");
        responseDto.setRole(Role.MECHANIC);

        when(userService.registerUser(any(UserRegisterDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("nuevoUser"));
    }

    @Test
    void login_Returns200_WhenValid() throws Exception {
        UserLoginDTO requestDto = new UserLoginDTO();
        requestDto.setEmail("test@test.com");
        requestDto.setPassword("12345678");

        TokenResponseDTO tokenResponse = new TokenResponseDTO("el-token-falso");

        when(userService.loginUser(any(UserLoginDTO.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("el-token-falso"));
    }
}
