package io.github.martinezdom.repairshop.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

import io.github.martinezdom.repairshop.dtos.CustomerCreateDTO;
import io.github.martinezdom.repairshop.dtos.CustomerResponseDTO;
import io.github.martinezdom.repairshop.dtos.CustomerUpdateDTO;
import io.github.martinezdom.repairshop.entities.Customer;
import io.github.martinezdom.repairshop.exceptions.CustomerAlreadyExistsException;
import io.github.martinezdom.repairshop.exceptions.CustomerNotFoundException;
import io.github.martinezdom.repairshop.exceptions.EmailAlreadyExists;
import io.github.martinezdom.repairshop.exceptions.PhoneAlreadyExists;
import io.github.martinezdom.repairshop.repositories.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_Success() {
        CustomerCreateDTO dto = new CustomerCreateDTO();
        dto.setEmail("cliente@taller.com");
        dto.setFirstName("Juan");
        dto.setLastName("Perez");
        dto.setPhone("123456789");

        when(customerRepository.existsByEmail("cliente@taller.com")).thenReturn(false);
        when(customerRepository.existsByPhone("123456789")).thenReturn(false);

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setEmail("cliente@taller.com");
        savedCustomer.setFirstName("Juan");
        savedCustomer.setLastName("Perez");
        savedCustomer.setPhone("123456789");

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponseDTO response = customerService.createCustomer(dto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Juan", response.getFirstName());
    }

    @Test
    void createCustomer_ThrowsException_WhenEmailExists() {
        CustomerCreateDTO dto = new CustomerCreateDTO();
        dto.setEmail("existe@taller.com");

        when(customerRepository.existsByEmail("existe@taller.com")).thenReturn(true);

        assertThrows(CustomerAlreadyExistsException.class, () -> {
            customerService.createCustomer(dto);
        });
    }

    @Test
    void createCustomer_ThrowsException_WhenPhoneExists() {
        CustomerCreateDTO dto = new CustomerCreateDTO();
        dto.setEmail("nuevo@taller.com");
        dto.setPhone("111111111");

        when(customerRepository.existsByEmail("nuevo@taller.com")).thenReturn(false);
        when(customerRepository.existsByPhone("111111111")).thenReturn(true);

        assertThrows(CustomerAlreadyExistsException.class, () -> {
            customerService.createCustomer(dto);
        });
    }

    @Test
    void getAllCustomers_ReturnsPage() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Maria");

        Page<Customer> fakePage = new PageImpl<>(List.of(customer));

        when(customerRepository.findAll(any(Pageable.class))).thenReturn(fakePage);

        Page<CustomerResponseDTO> result = customerService.getAllCustomers(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Maria", result.getContent().get(0).getFirstName());
    }

    @Test
    void getCustomerById_Success() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Pedro");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponseDTO response = customerService.getCustomerById(1L);

        assertEquals("Pedro", response.getFirstName());
    }

    @Test
    void getCustomerById_ThrowsException_WhenNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.getCustomerById(99L);
        });
    }

    @Test
    void deleteCustomer_Success() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        customerService.deleteCustomer(1L);

        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCustomer_ThrowsException_WhenNotFound() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.deleteCustomer(99L);
        });

        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateCustomer_Success() {
        CustomerUpdateDTO dto = new CustomerUpdateDTO();
        dto.setEmail("nuevo@taller.com");
        dto.setPhone("987654321");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setEmail("viejo@taller.com");
        existingCustomer.setPhone("123456789");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmail("nuevo@taller.com")).thenReturn(false);
        when(customerRepository.existsByPhone("987654321")).thenReturn(false);

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(1L);
        updatedCustomer.setEmail("nuevo@taller.com");
        updatedCustomer.setPhone("987654321");

        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        CustomerResponseDTO response = customerService.updateCustomer(1L, dto);

        assertEquals("nuevo@taller.com", response.getEmail());
    }

    @Test
    void updateCustomer_ThrowsException_WhenEmailAlreadyInUseByAnother() {
        CustomerUpdateDTO dto = new CustomerUpdateDTO();
        dto.setEmail("otro@taller.com");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setEmail("mio@taller.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmail("otro@taller.com")).thenReturn(true);

        assertThrows(EmailAlreadyExists.class, () -> {
            customerService.updateCustomer(1L, dto);
        });
    }
}
