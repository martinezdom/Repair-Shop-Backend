package io.github.martinezdom.repairshop.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import io.github.martinezdom.repairshop.dtos.RepairCreateDTO;
import io.github.martinezdom.repairshop.dtos.RepairResponseDTO;
import io.github.martinezdom.repairshop.dtos.RepairUpdateDTO;
import io.github.martinezdom.repairshop.entities.Repair;
import io.github.martinezdom.repairshop.entities.User;
import io.github.martinezdom.repairshop.entities.Vehicle;
import io.github.martinezdom.repairshop.enums.RepairStatus;
import io.github.martinezdom.repairshop.exceptions.InvalidStatusException;
import io.github.martinezdom.repairshop.exceptions.RepairNotFoundException;
import io.github.martinezdom.repairshop.exceptions.UserNotFoundException;
import io.github.martinezdom.repairshop.exceptions.VehicleNotFoundException;
import io.github.martinezdom.repairshop.repositories.RepairRepository;
import io.github.martinezdom.repairshop.repositories.UserRepository;
import io.github.martinezdom.repairshop.repositories.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RepairRepository repairRepository;

    @InjectMocks
    private RepairService repairService;

    @Test
    void createRepair_Success() {
        RepairCreateDTO dto = new RepairCreateDTO();
        dto.setVehicleId(1L);
        dto.setMechanicId(2L);
        dto.setDescription("Cambio de aceite");

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setLicensePlate("1111AAA");

        User mechanic = new User();
        mechanic.setId(2L);
        mechanic.setUsername("mecanico1");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mechanic));

        Repair savedRepair = new Repair();
        savedRepair.setId(1L);
        savedRepair.setDescription("Cambio de aceite");
        savedRepair.setVehicle(vehicle);
        savedRepair.setMechanic(mechanic);
        savedRepair.setStatus(RepairStatus.PENDIENTE);
        savedRepair.setEntryDate(LocalDateTime.now());

        when(repairRepository.save(any(Repair.class))).thenReturn(savedRepair);

        RepairResponseDTO response = repairService.createRepair(dto);

        assertEquals("Cambio de aceite", response.getDescription());
        assertEquals("mecanico1", response.getMechanicName());
        assertEquals(RepairStatus.PENDIENTE, response.getStatus());
    }

    @Test
    void createRepair_ThrowsException_WhenVehicleNotFound() {
        RepairCreateDTO dto = new RepairCreateDTO();
        dto.setVehicleId(99L);

        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> {
            repairService.createRepair(dto);
        });
    }

    @Test
    void createRepair_ThrowsException_WhenMechanicNotFound() {
        RepairCreateDTO dto = new RepairCreateDTO();
        dto.setVehicleId(1L);
        dto.setMechanicId(99L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            repairService.createRepair(dto);
        });
    }

    @Test
    void updateRepair_Success() {
        RepairUpdateDTO dto = new RepairUpdateDTO();
        dto.setStatus(RepairStatus.EN_PROGRESO);

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate("1111AAA");

        User mechanic = new User();
        mechanic.setUsername("mecanico1");

        Repair existingRepair = new Repair();
        existingRepair.setId(1L);
        existingRepair.setVehicle(vehicle);
        existingRepair.setMechanic(mechanic);

        when(repairRepository.findById(1L)).thenReturn(Optional.of(existingRepair));
        when(repairRepository.save(any(Repair.class))).thenReturn(existingRepair);

        RepairResponseDTO response = repairService.updateRepair(1L, dto);

        assertEquals(RepairStatus.EN_PROGRESO, response.getStatus());
    }

    @Test
    void getRepairsByStatus_Success() {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate("1111AAA");

        User mechanic = new User();
        mechanic.setUsername("mecanico1");
        
        Repair repair = new Repair();
        repair.setId(1L);
        repair.setStatus(RepairStatus.PENDIENTE);
        repair.setVehicle(vehicle);
        repair.setMechanic(mechanic);

        Page<Repair> fakePage = new PageImpl<>(List.of(repair));

        when(repairRepository.findByStatus(eq(RepairStatus.PENDIENTE), any(Pageable.class))).thenReturn(fakePage);

        Page<RepairResponseDTO> result = repairService.getRepairsByStatus("pendiente", 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(RepairStatus.PENDIENTE, result.getContent().get(0).getStatus());
    }

    @Test
    void getRepairsByStatus_ThrowsException_WhenInvalidStatus() {
        assertThrows(InvalidStatusException.class, () -> {
            repairService.getRepairsByStatus("INVENTADO", 0, 10);
        });
    }

    @Test
    void deleteRepair_Success() {
        when(repairRepository.existsById(1L)).thenReturn(true);
        repairService.deleteRepair(1L);
        verify(repairRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRepair_ThrowsException_WhenNotFound() {
        when(repairRepository.existsById(99L)).thenReturn(false);
        assertThrows(RepairNotFoundException.class, () -> {
            repairService.deleteRepair(99L);
        });
        verify(repairRepository, never()).deleteById(anyLong());
    }
}
