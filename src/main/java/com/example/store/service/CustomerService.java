package com.example.store.service;

import com.example.store.dto.CustomerCreateDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Cacheable("customers")
    public List<CustomerDTO> getAllCustomers() {
        log.info("Fetching all customers");
        List<CustomerDTO> customers = customerMapper.customersToCustomerDTOs(customerRepository.findAll());
        log.debug("Fetched {} customers", customers.size());
        return customers;
    }

    public Page<CustomerDTO> getAllCustomers(int page, int size) {
        log.info("Fetching customers page={} size={}", page, size);
        Page<CustomerDTO> paged = customerRepository.findAll(PageRequest.of(page, size))
                .map(customerMapper::customerToCustomerDTO);
        log.debug("Fetched {} customers in page {}", paged.getContent().size(), page);
        return paged;
    }

    public List<CustomerDTO> searchCustomers(String query) {
        log.info("Searching customers by query: {}", query);
        List<Customer> customers = customerRepository.findByNameContainingIgnoreCase(query);
        if (customers.isEmpty()) {
            log.warn("No customers found matching query '{}'", query);
        }
        return customerMapper.customersToCustomerDTOs(customers);
    }

    public CustomerDTO createCustomer(CustomerCreateDTO dto) {
        log.info("Creating new customer: {}", dto.getName());
        Customer entity = customerMapper.customerCreateDtoToCustomer(dto);
        Customer saved = customerRepository.save(entity);
        log.info("Customer created with id={}", saved.getId());
        return customerMapper.customerToCustomerDTO(saved);
    }

    public CustomerDTO getCustomerById(Long id) {
        log.info("Fetching customer by id={}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Customer not found with id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
                });
        return customerMapper.customerToCustomerDTO(customer);
    }
}
