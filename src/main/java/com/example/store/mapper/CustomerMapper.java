package com.example.store.mapper;

import com.example.store.dto.CustomerCreateDTO;
import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;

import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer customerCreateDtoToCustomer(CustomerCreateDTO dto);
    CustomerDTO customerToCustomerDTO(Customer customer);

    List<CustomerDTO> customersToCustomerDTOs(List<Customer> customer);

    default List<Long> mapOrderIds(Customer customer) {
        return customer.getOrders() == null ? List.of() :
                customer.getOrders().stream().map(o -> o.getId()).collect(Collectors.toList());
    }
}
