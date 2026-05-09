package io.github.martinezdom.repairshop.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.martinezdom.repairshop.dtos.UserRegisterDTO;
import io.github.martinezdom.repairshop.dtos.UserResponseDTO;
import io.github.martinezdom.repairshop.dtos.UserLoginDTO;
import io.github.martinezdom.repairshop.dtos.TokenResponseDTO;
import io.github.martinezdom.repairshop.entities.User;
import io.github.martinezdom.repairshop.enums.Role;
import io.github.martinezdom.repairshop.exceptions.EmailAlreadyExists;
import io.github.martinezdom.repairshop.exceptions.UserNotFoundException;
import io.github.martinezdom.repairshop.exceptions.UsernameAlreadyExistsException;
import io.github.martinezdom.repairshop.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_Success() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("paco");
        dto.setEmail("paco@taller.com");
        dto.setPassword("1234");

        when(userRepository.existsByUsername("paco")).thenReturn(false);
        when(userRepository.existsByEmail("paco@taller.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("hashed_password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("paco");
        savedUser.setEmail("paco@taller.com");
        savedUser.setRole(Role.ADMIN);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO response = userService.registerUser(dto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("paco", response.getUsername());
        assertEquals(Role.ADMIN, response.getRole());
        
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsException_WhenEmailExists() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("paco_nuevo");
        dto.setEmail("paco@taller.com");
        dto.setPassword("1234");

        when(userRepository.existsByUsername("paco_nuevo")).thenReturn(false);
        when(userRepository.existsByEmail("paco@taller.com")).thenReturn(true);

        assertThrows(EmailAlreadyExists.class, () -> {
            userService.registerUser(dto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ThrowsException_WhenUsernameExists() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("paco");
        dto.setEmail("paco_nuevo@taller.com");
        dto.setPassword("1234");

        when(userRepository.existsByUsername("paco")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.registerUser(dto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_Success() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("paco@taller.com");
        dto.setPassword("1234");

        User user = new User();
        user.setEmail("paco@taller.com");
        user.setPasswordHash("hashed_password");

        when(userRepository.findByEmail("paco@taller.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("1234", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        TokenResponseDTO response = userService.loginUser(dto);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
    }

    @Test
    void loginUser_ThrowsException_WhenEmailNotFound() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("noexiste@taller.com");
        dto.setPassword("1234");

        when(userRepository.findByEmail("noexiste@taller.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.loginUser(dto);
        });
    }

    @Test
    void loginUser_ThrowsException_WhenPasswordInvalid() {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setEmail("paco@taller.com");
        dto.setPassword("malpassword");

        User user = new User();
        user.setEmail("paco@taller.com");
        user.setPasswordHash("hashed_password");

        when(userRepository.findByEmail("paco@taller.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("malpassword", "hashed_password")).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> {
            userService.loginUser(dto);
        });
    }

    @Test
    void getAllUsers_ReturnsPage() {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("mecanico1");
        fakeUser.setRole(Role.MECHANIC);

        Page<User> fakePage = new PageImpl<>(List.of(fakeUser));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(fakePage);

        Page<UserResponseDTO> result = userService.getAllUsers(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("mecanico1", result.getContent().get(0).getUsername());
    }

    @Test
    void getUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("paco");
        user.setRole(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals("paco", response.getUsername());
    }

    @Test
    void getUserById_ThrowsException_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(99L);
        });
    }
}
