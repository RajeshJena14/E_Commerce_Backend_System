package com.incture.E_Commerce_Backend_System.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import com.incture.E_Commerce_Backend_System.dto.OrderResponseDto;
import com.incture.E_Commerce_Backend_System.dto.OrdersRequestDto;
import com.incture.E_Commerce_Backend_System.dto.UserResponseDto;
import com.incture.E_Commerce_Backend_System.entity.Cart;
import com.incture.E_Commerce_Backend_System.entity.CartItem;
import com.incture.E_Commerce_Backend_System.entity.EmailDetails;
import com.incture.E_Commerce_Backend_System.entity.OrderItems;
import com.incture.E_Commerce_Backend_System.entity.Orders;
import com.incture.E_Commerce_Backend_System.entity.Product;
import com.incture.E_Commerce_Backend_System.entity.User;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.repository.CartRepository;
import com.incture.E_Commerce_Backend_System.repository.OrdersRepository;
import com.incture.E_Commerce_Backend_System.repository.ProductRepository;
import com.incture.E_Commerce_Backend_System.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrdersRepository ordersRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private CartRepository cartRepository;

	@Mock
	private EmailService emailService;

	@Spy
	private ModelMapper modelMapper = new ModelMapper();

	@InjectMocks
	private OrderService orderService;

	private User mockCustomer;
	private UserResponseDto mockCustomerUser;
	private UserResponseDto mockAdminUser;
	private Product mockProduct;
	private Cart mockCart;
	private Orders mockOrder;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Order Service...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Order Service...");
	}

	@BeforeEach
	void setUp() {
		mockCustomer = new User();
		ReflectionTestUtils.setField(mockCustomer, "id", 1L);
		mockCustomer.setName("JohnDoe");
		mockCustomer.setEmail("john@test.com");
		mockCustomer.setRole("CUSTOMER");

		mockCustomerUser = new UserResponseDto();
		mockCustomerUser.setId(1L);
		mockCustomerUser.setRole("CUSTOMER");

		mockAdminUser = new UserResponseDto();
		mockAdminUser.setId(99L);
		mockAdminUser.setRole("ADMIN");

		mockProduct = new Product();
		ReflectionTestUtils.setField(mockProduct, "id", 100L);
		mockProduct.setName("Test Laptop");
		mockProduct.setPrice(1000.0);
		mockProduct.setStock(5);

		mockCart = new Cart();
		ReflectionTestUtils.setField(mockCart, "id", 50L);
		mockCart.setUser(mockCustomer);
		mockCart.setTotal_price(2000.0);
		CartItem cartItem = new CartItem(mockCart, mockProduct, 2);
		mockCart.setCartItems(new ArrayList<>(List.of(cartItem)));

		mockOrder = new Orders();
		ReflectionTestUtils.setField(mockOrder, "id", 500L);
		mockOrder.setUser(mockCustomer);
		mockOrder.setOrder_status("PLACED");
		mockOrder.setTotal_amount(2000.0);
		mockOrder.setOrderDate(LocalDateTime.now());
		mockOrder.setOrderItems(new ArrayList<>());
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
		mockCustomer = null;
		mockCustomerUser = null;
		mockAdminUser = null;
		mockProduct = null;
		mockOrder = null;
		mockCart = null;
	}

	private void mockSecurityContext(UserResponseDto principal) {
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication authentication = mock(Authentication.class);

		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(principal);

		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	@DisplayName("Test for Checkout - Fails due to Missing Auth")
	void testCheckout_AuthFailure() {
		assertThrows(AuthenticationCredentialsNotFoundException.class, () -> orderService.checkoutFromCart(2000.0));
	}

	@Test
	@DisplayName("Test for Checkout - Fails due to User missing from DB")
	void testCheckout_UserNotFound() {
		mockSecurityContext(mockCustomerUser);

		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> orderService.checkoutFromCart(2000.0));
	}

	@Test
	@DisplayName("Test for Checkout - Fails due to Empty Cart")
	void testCheckout_EmptyCart() {
		mockSecurityContext(mockCustomerUser);

		when(userRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
		when(cartRepository.findByUserId(1L)).thenReturn(null);

		CustomException ex = assertThrows(CustomException.class, () -> orderService.checkoutFromCart(2000.0));
		assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
	}

	@Test
	@DisplayName("Test for Checkout - Fails due to Insufficient Stock")
	void testCheckout_InsufficientStock() {
		mockSecurityContext(mockCustomerUser);
		mockProduct.setStock(1); // Stock = 1, but required = 2!

		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(userRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

		CustomException ex = assertThrows(CustomException.class, () -> orderService.checkoutFromCart(2000.0));
		assertTrue(ex.getMessage().contains("out of stock"));
	}

	@Test
	@DisplayName("Test for Checkout - Payment Failed (Saved as FAILED and Stock Restored)")
	void testCheckout_PaymentFailed() {
		mockSecurityContext(mockCustomerUser);
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(userRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
		when(ordersRepository.save(ArgumentMatchers.any(Orders.class)))
				.thenAnswer(passedArguments -> passedArguments.getArgument(0));

		// Total = 2000, but Payment done = 1500
		OrderResponseDto result = orderService.checkoutFromCart(1500.0);

		assertNotNull(result);
		assertEquals("FAILED", result.getPayment_status());
		assertEquals("FAILED", result.getOrder_status());

		assertEquals(5, mockProduct.getStock());

		verify(ordersRepository, times(1)).save(ArgumentMatchers.any(Orders.class));
		verify(cartRepository, never()).delete(ArgumentMatchers.any(Cart.class));
		verify(emailService, never()).sendSimpleMail(ArgumentMatchers.any());
	}

	@Test
	@DisplayName("Test for Checkout - Success (Order Placed, Cart Cleared and Email Sent)")
	void testCheckout_Success() {
		mockSecurityContext(mockCustomerUser);
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(userRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
		when(ordersRepository.save(ArgumentMatchers.any(Orders.class)))
				.thenAnswer(passedArguments -> passedArguments.getArgument(0));

		OrderResponseDto result = orderService.checkoutFromCart(2000.0);

		assertNotNull(result);
		assertEquals("SUCCESSFUL", result.getPayment_status());
		assertEquals("PLACED", result.getOrder_status());

		// Stock deduction check
		assertEquals(3, mockProduct.getStock());

		verify(cartRepository, times(1)).delete(mockCart);
		verify(emailService, times(1)).sendSimpleMail(ArgumentMatchers.any(EmailDetails.class));
	}

	@Test
	@DisplayName("Test for Getting All Orders - Admin Fetch (Gets everything)")
	void testGetAllOrders_Admin() {
		mockSecurityContext(mockAdminUser);
		Pageable pageable = PageRequest.of(0, 5);
		when(ordersRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(mockOrder)));

		Page<OrderResponseDto> result = orderService.getAllOrders(pageable);

		assertEquals(1, result.getTotalElements());
		verify(ordersRepository, times(1)).findAll(pageable);
	}

	@Test
	@DisplayName("Test for Getting All Orders - Customer Fetch (Gets only theirs)")
	void testGetAllOrders_Customer() {
		mockSecurityContext(mockCustomerUser);
		Pageable pageable = PageRequest.of(0, 5);
		when(ordersRepository.findByUserId(1L, pageable)).thenReturn(new PageImpl<>(List.of(mockOrder)));

		Page<OrderResponseDto> result = orderService.getAllOrders(pageable);
		assertEquals(1, result.getTotalElements());
		verify(ordersRepository, times(1)).findByUserId(1L, pageable);
	}

	@Test
	@DisplayName("Test for Getting Orders History - Success")
	void testGetOrdersHistory_Success() {
		mockSecurityContext(mockCustomerUser);
		Pageable pageable = PageRequest.of(0, 5);
		when(ordersRepository.findByUserId(1L, pageable)).thenReturn(new PageImpl<>(List.of(mockOrder)));

		Page<OrderResponseDto> result = orderService.getOrdersHistory(pageable);
		assertEquals(1, result.getTotalElements());
	}

	@Test
	@DisplayName("Test for Getting Order Details - Not Found")
	void testGetOrderDetailsById_NotFound() {
		mockSecurityContext(mockCustomerUser);
		OrdersRequestDto req = new OrdersRequestDto();
		req.setId(999L);
		when(ordersRepository.findById(999L)).thenReturn(Optional.empty());

		assertNull(orderService.getOrderDetailsById(req));
	}

	@Test
	@DisplayName("Test for Getting Order Details - Access Denied (Customer spying on another's order)")
	void testGetOrderDetailsById_AccessDenied() {
		mockSecurityContext(mockCustomerUser); // ID 1

		// Order is of user ID 2
		User otherUser = new User();
		ReflectionTestUtils.setField(otherUser, "id", 2L);
		mockOrder.setUser(otherUser);

		OrdersRequestDto req = new OrdersRequestDto();
		req.setId(500L);
		when(ordersRepository.findById(500L)).thenReturn(Optional.of(mockOrder));

		assertThrows(AccessDeniedException.class, () -> orderService.getOrderDetailsById(req));
	}

	@Test
	@DisplayName("Test for Getting Order Details - Success (Admin checking any order)")
	void testGetOrderDetailsById_AdminSuccess() {
		mockSecurityContext(mockAdminUser);
		OrdersRequestDto req = new OrdersRequestDto();
		req.setId(500L);

		when(ordersRepository.findById(500L)).thenReturn(Optional.of(mockOrder));

		OrderResponseDto result = orderService.getOrderDetailsById(req);
		assertNotNull(result);
	}

	@Test
	@DisplayName("Test for Updating Status - Order Not Found")
	void testUpdateOrderStatus_NotFound() {
		OrdersRequestDto req = new OrdersRequestDto();
		req.setId(999L);
		when(ordersRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> orderService.updateOrderStatusById(req));
	}

	@Test
	@DisplayName("Test for Updating Status - Illegal Status String")
	void testUpdateOrderStatus_IllegalStatus() {
		OrdersRequestDto req = new OrdersRequestDto();
		req.setId(500L);
		req.setOrderStatus("SUPER_FAST_DELIVERY"); // Invalid status

		when(ordersRepository.findById(500L)).thenReturn(Optional.of(mockOrder));

		assertThrows(IllegalArgumentException.class, () -> orderService.updateOrderStatusById(req));
	}

	@Test
	@DisplayName("Test for Updating Status - PLACED to CANCELLED (Success & Stock Restored)")
	void testUpdateOrderStatus_CancelSuccess() {
		OrdersRequestDto req = new OrdersRequestDto();
		req.setId(500L);
		req.setOrderStatus("CANCELLED");

		OrderItems orderItem = new OrderItems(mockOrder, mockProduct, 2, 2000.0);
		mockOrder.setOrderItems(new ArrayList<>(List.of(orderItem)));

		when(ordersRepository.findById(500L)).thenReturn(Optional.of(mockOrder));
		when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));

		when(ordersRepository.save(ArgumentMatchers.any(Orders.class)))
				.thenAnswer(passedArgs -> passedArgs.getArgument(0));

		assertEquals(5, mockProduct.getStock()); // Old Stock Check

		OrderResponseDto result = orderService.updateOrderStatusById(req);

		assertNotNull(result);
		assertEquals("CANCELLED", result.getOrder_status());

		assertEquals(7, mockProduct.getStock()); // New Stock Check
	}

	@Test
	@DisplayName("Test for Updating Status - Invalid Transition (SHIPPED to CANCELLED)")
	void testUpdateOrderStatus_CancelFails() {
		mockOrder.setOrder_status("SHIPPED");
		OrdersRequestDto req = new OrdersRequestDto();
		req.setId(500L);
		req.setOrderStatus("CANCELLED");

		when(ordersRepository.findById(500L)).thenReturn(Optional.of(mockOrder));

		assertNull(orderService.updateOrderStatusById(req));
	}

	@Test
	@DisplayName("Test for Updating Status - PLACED to SHIPPED (Success)")
	void testUpdateOrderStatus_ShippedSuccess() {
		OrdersRequestDto req = new OrdersRequestDto();
		req.setId(500L);
		req.setOrderStatus("SHIPPED");

		when(ordersRepository.findById(500L)).thenReturn(Optional.of(mockOrder));
		when(ordersRepository.save(ArgumentMatchers.any(Orders.class)))
				.thenAnswer(passedArgs -> passedArgs.getArgument(0));

		OrderResponseDto result = orderService.updateOrderStatusById(req);
		assertEquals("SHIPPED", result.getOrder_status());
	}

	@Test
	@DisplayName("Test for Updating Status - SHIPPED to DELIVERED (Success)")
	void testUpdateOrderStatus_DeliveredSuccess() {
		mockOrder.setOrder_status("SHIPPED");
		OrdersRequestDto req = new OrdersRequestDto();
		req.setId(500L);
		req.setOrderStatus("DELIVERED");

		when(ordersRepository.findById(500L)).thenReturn(Optional.of(mockOrder));
		when(ordersRepository.save(ArgumentMatchers.any(Orders.class)))
				.thenAnswer(passedArgs -> passedArgs.getArgument(0));

		OrderResponseDto result = orderService.updateOrderStatusById(req);
		assertEquals("DELIVERED", result.getOrder_status());
	}

}
