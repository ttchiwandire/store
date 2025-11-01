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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderMapper orderMapper;
    private CustomerRepository customerRepository;
    private ProductRepository productRepository;

    private OrderService orderService;

    private Order order;
    private OrderDTO orderDTO;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        orderMapper = mock(OrderMapper.class);
        customerRepository = mock(CustomerRepository.class);
        productRepository = mock(ProductRepository.class);

        orderService = new OrderService(orderRepository, orderMapper, customerRepository, productRepository);

        customer = new Customer();
        customer.setId(1L);
        customer.setName("Tatenda");

        product = new Product();
        product.setId(100L);
        product.setDescription("Laptop");

        order = new Order();
        order.setId(10L);
        order.setDescription("Order for Laptop");
        order.setCustomer(customer);
        order.setProducts(List.of(product));

        orderDTO = new OrderDTO();
        orderDTO.setId(10L);
        orderDTO.setDescription("Order for Laptop");
    }

    // ----------------------------------------------------------
    // getAllOrders()
    // ----------------------------------------------------------
    @Test
    @DisplayName("Should return all orders successfully")
    void shouldReturnAllOrders() {
        given(orderRepository.findAll()).willReturn(List.of(order));
        given(orderMapper.ordersToOrderDTOs(List.of(order))).willReturn(List.of(orderDTO));

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Order for Laptop");
        verify(orderRepository).findAll();
        verify(orderMapper).ordersToOrderDTOs(anyList());
    }

    @Test
    @DisplayName("Should return empty list when no orders exist")
    void shouldReturnEmptyListWhenNoOrders() {
        given(orderRepository.findAll()).willReturn(List.of());
        given(orderMapper.ordersToOrderDTOs(List.of())).willReturn(List.of());

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).isEmpty();
        verify(orderRepository).findAll();
    }

    // ----------------------------------------------------------
    // getOrderById()
    // ----------------------------------------------------------
    @Test
    @DisplayName("Should return order by ID successfully")
    void shouldReturnOrderById() {
        given(orderRepository.findById(10L)).willReturn(Optional.of(order));
        given(orderMapper.orderToOrderDTO(order)).willReturn(orderDTO);

        OrderDTO result = orderService.getOrderById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        verify(orderRepository).findById(10L);
    }

    @Test
    @DisplayName("Should throw 404 when order not found")
    void shouldThrowNotFoundWhenOrderMissing() {
        given(orderRepository.findById(99L)).willReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> orderService.getOrderById(99L),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).isEqualTo("Order not found");
        verify(orderRepository).findById(99L);
    }

    // ----------------------------------------------------------
    // createOrder()
    // ----------------------------------------------------------
    @Test
    @DisplayName("Should create order successfully with valid customer and products")
    void shouldCreateOrderSuccessfully() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setDescription("Buy laptop");
        dto.setCustomerId(1L);
        dto.setProductIds(List.of(100L));

        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        given(productRepository.findAllById(List.of(100L))).willReturn(List.of(product));
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderMapper.orderToOrderDTO(order)).willReturn(orderDTO);

        OrderDTO result = orderService.createOrder(dto);

        assertThat(result.getDescription()).isEqualTo("Order for Laptop");

        verify(orderRepository).save(any(Order.class));
        verify(customerRepository).findById(1L);
        verify(productRepository).findAllById(List.of(100L));
    }

    @Test
    @DisplayName("Should throw 400 BAD_REQUEST when customer ID is invalid")
    void shouldThrowBadRequestWhenCustomerInvalid() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setDescription("Test order");
        dto.setCustomerId(999L);

        given(customerRepository.findById(999L)).willReturn(Optional.empty());

        ResponseStatusException ex = catchThrowableOfType(
                () -> orderService.createOrder(dto),
                ResponseStatusException.class
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("Invalid customer ID");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create order even if product list is null")
    void shouldCreateOrderWhenProductIdsNull() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setDescription("Empty order");
        dto.setCustomerId(1L);
        dto.setProductIds(null);

        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderMapper.orderToOrderDTO(order)).willReturn(orderDTO);

        OrderDTO result = orderService.createOrder(dto);

        assertThat(result.getId()).isEqualTo(10L);
        verify(orderRepository).save(any(Order.class));
        verify(productRepository, never()).findAllById(anyList());
    }

    @Test
    @DisplayName("Should correctly map DTO fields to Order entity before saving")
    void shouldMapDtoToOrderBeforeSave() {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setDescription("Office purchase");
        dto.setCustomerId(1L);
        dto.setProductIds(List.of(100L));

        given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
        given(productRepository.findAllById(anyList())).willReturn(List.of(product));
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderMapper.orderToOrderDTO(order)).willReturn(orderDTO);

        orderService.createOrder(dto);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order captured = captor.getValue();

        assertThat(captured.getDescription()).isEqualTo("Office purchase");
        assertThat(captured.getCustomer().getId()).isEqualTo(1L);
        assertThat(captured.getProducts()).hasSize(1);
    }

    // ----------------------------------------------------------
    // Logging and robustness
    // ----------------------------------------------------------
    @Nested
    @DisplayName("Logging and defensive checks")
    class LoggingBehavior {

        @Test
        @DisplayName("Should log creation and repository calls without exceptions")
        void shouldLogSuccessfully() {
            OrderCreateDTO dto = new OrderCreateDTO();
            dto.setDescription("Keyboard order");
            dto.setCustomerId(1L);
            dto.setProductIds(List.of(100L));

            given(customerRepository.findById(1L)).willReturn(Optional.of(customer));
            given(productRepository.findAllById(List.of(100L))).willReturn(List.of(product));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(orderMapper.orderToOrderDTO(order)).willReturn(orderDTO);

            OrderDTO result = orderService.createOrder(dto);

            assertThat(result).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }
    }
}
