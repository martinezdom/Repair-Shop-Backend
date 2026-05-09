package io.github.martinezdom.repairshop.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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

import io.github.martinezdom.repairshop.dtos.VehicleCreateDTO;
import io.github.martinezdom.repairshop.dtos.VehicleResponseDTO;
import io.github.martinezdom.repairshop.dtos.VehicleUpdateDTO;
import io.github.martinezdom.repairshop.entities.Customer;
import io.github.martinezdom.repairshop.entities.Vehicle;
import io.github.martinezdom.repairshop.exceptions.CustomerNotFoundException;
import io.github.martinezdom.repairshop.exceptions.VehicleAlreadyExistsException;
import io.github.martinezdom.repairshop.exceptions.VehicleNotFoundException;
import io.github.martinezdom.repairshop.repositories.CustomerRepository;
import io.github.martinezdom.repairshop.repositories.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void createVehicle_Success() {
        VehicleCreateDTO dto = new VehicleCreateDTO();
        dto.setLicensePlate("1234ABC");
        dto.setCustomerId(1L);
        dto.setBrand("Ford");

        Customer owner = new Customer();
        owner.setId(1L);
        owner.setFirstName("Ana");

        when(vehicleRepository.existsByLicensePlate("1234ABC")).thenReturn(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(owner));

        Vehicle savedVehicle = new Vehicle();
        savedVehicle.setId(1L);
        savedVehicle.setLicensePlate("1234ABC");
        savedVehicle.setBrand("Ford");
        savedVehicle.setOwner(owner);

        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleResponseDTO response = vehicleService.createVehicle(dto);

        assertEquals("1234ABC", response.getLicensePlate());
        assertEquals(1L, response.getCustomerId());
    }

    @Test
    void createVehicle_ThrowsException_WhenLicensePlateExists() {
        VehicleCreateDTO dto = new VehicleCreateDTO();
        dto.setLicensePlate("EXISTE1");

        when(vehicleRepository.existsByLicensePlate("EXISTE1")).thenReturn(true);

        assertThrows(VehicleAlreadyExistsException.class, () -> {
            vehicleService.createVehicle(dto);
        });
    }

    @Test
    void createVehicle_ThrowsException_WhenCustomerNotFound() {
        VehicleCreateDTO dto = new VehicleCreateDTO();
        dto.setLicensePlate("NUEVA2");
        dto.setCustomerId(99L);

        when(vehicleRepository.existsByLicensePlate("NUEVA2")).thenReturn(false);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            vehicleService.createVehicle(dto);
        });
    }

    @Test
    void getAllVehicles_ReturnsPage() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setLicensePlate("1234ABC");
        
        Customer owner = new Customer();
        owner.setId(1L);
        owner.setFirstName("Ana");
        owner.setLastName("Perez");
        vehicle.setOwner(owner);

        Page<Vehicle> fakePage = new PageImpl<>(List.of(vehicle));

        when(vehicleRepository.findAll(any(Pageable.class))).thenReturn(fakePage);

        Page<VehicleResponseDTO> result = vehicleService.getAllVehicles(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Ana Perez", result.getContent().get(0).getCustomerName());
    }

    @Test
    void getVehicleById_Success() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setLicensePlate("1234ABC");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleResponseDTO response = vehicleService.getVehicleById(1L);

        assertEquals("1234ABC", response.getLicensePlate());
    }

    @Test
    void getVehicleById_ThrowsException_WhenNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> {
            vehicleService.getVehicleById(99L);
        });
    }

    @Test
    void deleteVehicle_Success() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteVehicle_ThrowsException_WhenNotFound() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        assertThrows(VehicleNotFoundException.class, () -> {
            vehicleService.deleteVehicle(99L);
        });

        verify(vehicleRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateVehicle_Success() {
        VehicleUpdateDTO dto = new VehicleUpdateDTO();
        dto.setBrand("Toyota");

        Customer owner = new Customer();
        owner.setId(1L);

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.setId(1L);
        existingVehicle.setLicensePlate("1234ABC");
        existingVehicle.setBrand("Ford");
        existingVehicle.setOwner(owner);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));

        Vehicle updatedVehicle = new Vehicle();
        updatedVehicle.setId(1L);
        updatedVehicle.setLicensePlate("1234ABC");
        updatedVehicle.setBrand("Toyota");
        updatedVehicle.setOwner(owner);

        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(updatedVehicle);

        VehicleResponseDTO response = vehicleService.updateVehicle(1L, dto);

        assertEquals("Toyota", response.getBrand());
    }
}
