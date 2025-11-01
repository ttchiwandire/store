package com.example.store.service;

import com.example.store.dto.CustomerCreateDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

class CustomerServiceTest {

    private CustomerRepository customerRepository;
    private CustomerMapper customerMapper;
    private CustomerService customerService;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setup() {
        customerRepository = mock(CustomerRepository.class);
        customerMapper = mock(CustomerMapper.class);
        customerService = new CustomerService(customerRepository, customerMapper);

        customer = new Customer();
        customer.setId(1L);
        customer.setName("Tatenda");

        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("Tatenda");
    }

    // -------------------------------------------------------
    // getAllCustomers() [@Cacheable]
    // -------------------------------------------------------
    @Test
    @DisplayName("Should return all customers successfully")
    void shouldReturnAllCustomers() {
        given(customerRepository.findAll()).willReturn(List.of(customer));
        given(customerMapper.customersToCustomerDTOs(List.of(customer)))
                .willReturn(List.of(customerDTO));

        List<CustomerDTO> result = customerService.getAllCustomers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Tatenda");
        verify(customerRepository, times(1)).findAll();
        verify(customerMapper, times(1)).customersToCustomerDTOs(anyList());
    }

    // -------------------------------------------------------
    // getAllCustomers(page, size)
    // -------------------------------------------------------
    @Test
    @DisplayName("Should return paged customers successfully")
    void shouldReturnPagedCustomers() {
        Page<Customer> pageResult = new PageImpl<>(List.of(customer));
        given(customerRepository.findAll(any(PageRequest.class)))
                .willReturn(pageResult);
        given(customerMapper.customerToCustomerDTO(customer))
                .willReturn(customerDTO);

        Page<CustomerDTO> result = customerService.getAllCustomers(0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(customerRepository).findAll(PageRequest.of(0, 10));
    }

    // -------------------------------------------------------
    // searchCustomers(query)
    // -------------------------------------------------------
    @Test
    @DisplayName("Should search customers by name and return results")
    void shouldSearchCustomersSuccessfully() {
        given(customerRepository.findByNameContainingIgnoreCase("tate"))
                .willReturn(List.of(customer));
        given(customerMapper.customersToCustomerDTOs(List.of(customer)))
                .willReturn(List.of(customerDTO));

        List<CustomerDTO> result = customerService.searchCustomers("tate");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Tatenda");
        verify(customerRepository).findByNameContainingIgnoreCase("tate");
    }

    @Test
    @DisplayName("Should return empty list when search has no matches")
    void shouldReturnEmptyWhenSearchNoMatches() {
        given(customerRepository.findByNameContainingIgnoreCase("missing"))
                .willReturn(List.of());
        given(customerMapper.customersToCustomerDTOs(List.of()))
                .willReturn(List.of());

        List<CustomerDTO> result = customerService.searchCustomers("missing");

        assertThat(result).isEmpty();
        verify(customerRepository).findByNameContainingIgnoreCase("missing");
    }

    // -------------------------------------------------------
    // createCustomer(dto)
    // -------------------------------------------------------
    @Test
    @DisplayName("Should create new customer successfully")
    void shouldCreateCustomerSuccessfully() {
        CustomerCreateDTO dto = new CustomerCreateDTO();
        dto.setName("Alice");

        given(customerMapper.customerCreateDtoToCustomer(dto)).willReturn(customer);
        given(customerRepository.save(any(Customer.class))).willReturn(customer);
        given(customerMapper.customerToCustomerDTO(customer)).willReturn(customerDTO);

        CustomerDTO result = customerService.createCustomer(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should pass correct Customer entity to repository when creating")
    void shouldPassMappedEntityToRepository() {
        CustomerCreateDTO dto = new CustomerCreateDTO();
        dto.setName("Bob");

        Customer newCustomer = new Customer();
        newCustomer.setName("Bob");

        given(customerMapper.customerCreateDtoToCustomer(dto)).willReturn(newCustomer);
        given(customerRepository.save(any(Customer.class))).willReturn(newCustomer);
        given(customerMapper.customerToCustomerDTO(any(Customer.class))).willReturn(customerDTO);

        customerService.createCustomer(dto);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Bob");
    }

    // -------------------------------------------------------
    // getCustomerById(id)
    // -------------------------------------------------------
    @Test
    @DisplayName("Should return customer by ID successfully")
    void shouldReturnCustomerById() {
        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        given(customerMapper.customerToCustomerDTO(customer)).willReturn(customerDTO);

        CustomerDTO result = customerService.getCustomerById(1L);

        assertThat(result.getName()).isEqualTo("Tatenda");
        verify(customerRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw 404 ResponseStatusException when customer not found")
    void shouldThrowNotFoundWhenCustomerMissing() {
        given(customerRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> customerService.getCustomerById(99L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).isEqualTo("Customer not found");
        verify(customerRepository).findById(99L);
    }

    @Nested
    @DisplayName("Logging Verification")
    class LoggingBehavior {

        @Test
        @DisplayName("Should log when creating customer")
        void shouldLogWhenCreatingCustomer() {
            CustomerCreateDTO dto = new CustomerCreateDTO();
            dto.setName("Zara");

            given(customerMapper.customerCreateDtoToCustomer(dto)).willReturn(customer);
            given(customerRepository.save(any(Customer.class))).willReturn(customer);
            given(customerMapper.customerToCustomerDTO(any(Customer.class))).willReturn(customerDTO);

            customerService.createCustomer(dto);
            verify(customerRepository, times(1)).save(any(Customer.class));
        }
    }
}
