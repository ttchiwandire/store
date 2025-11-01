package com.example.store.service;

import com.example.store.dto.OrderCreateDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public List<OrderDTO> getAllOrders() {
        log.info("Fetching all orders");
        List<OrderDTO> orders = orderMapper.ordersToOrderDTOs(orderRepository.findAll());
        log.debug("Fetched {} orders", orders.size());
        return orders;
    }

    public OrderDTO getOrderById(Long id) {
        log.info("Fetching order by id={}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
                });
        return orderMapper.orderToOrderDTO(order);
    }

    public OrderDTO createOrder(OrderCreateDTO dto) {
        log.info("Creating order for customerId={} with products={}",
                dto.getCustomerId(), dto.getProductIds());

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid customer ID"));

        List<Product> products = dto.getProductIds() == null
                ? List.of()
                : productRepository.findAllById(dto.getProductIds());

        Order order = new Order();
        order.setDescription(dto.getDescription());
        order.setCustomer(customer);
        order.setProducts(products);

        Order saved = orderRepository.save(order);
        log.info("Order created with id={} for customerId={}", saved.getId(), dto.getCustomerId());
        return orderMapper.orderToOrderDTO(saved);
    }
}
