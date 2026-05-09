package io.github.martinezdom.repairshop.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.martinezdom.repairshop.entities.User;
import io.github.martinezdom.repairshop.enums.Role;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "M1_CL4V3_S3CR3T4_M1_CL4V3_S3CR3T4_M1_CL4V3_S3CR3T4");
    }

    @Test
    void generateToken_CreatesValidToken() {
        User user = new User();
        user.setEmail("admin@taller.com");
        user.setRole(Role.ADMIN);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateTokenAndGetEmail_ReturnsEmail_WhenValid() {
        User user = new User();
        user.setEmail("admin@taller.com");
        user.setRole(Role.ADMIN);

        String token = jwtService.generateToken(user);

        String extractedEmail = jwtService.validateTokenAndGetEmail(token);

        assertEquals("admin@taller.com", extractedEmail);
    }

    @Test
    void validateTokenAndGetEmail_ReturnsNull_WhenInvalidToken() {
        String invalidToken = "este.token.esfalso";

        String extractedEmail = jwtService.validateTokenAndGetEmail(invalidToken);

        assertNull(extractedEmail);
    }
}
