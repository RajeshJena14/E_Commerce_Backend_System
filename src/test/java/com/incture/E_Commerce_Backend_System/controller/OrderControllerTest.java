package com.incture.E_Commerce_Backend_System.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.incture.E_Commerce_Backend_System.dto.OrderResponseDto;
import com.incture.E_Commerce_Backend_System.dto.OrdersRequestDto;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

	private MockMvc mockMvc;

	@Mock
	private OrderService orderService;

	@InjectMocks
	private OrderController orderController;

	private OrderResponseDto mockResponseDto;

	private Page<OrderResponseDto> mockPage;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Order Controller...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Order Controller...");
	}

	@BeforeEach
	void setUp() throws Exception {
		mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();

		mockResponseDto = new OrderResponseDto();
		mockResponseDto.setId(500L);
		mockResponseDto.setTotal_amount(1500.00);
		mockResponseDto.setPayment_status("SUCCESS");
		mockResponseDto.setOrder_status("SHIPPED");
		mockResponseDto.setOrderDate(LocalDateTime.now());

		Pageable testPageable = PageRequest.of(0, 5, Sort.by("id").ascending());
		mockPage = new PageImpl<>(List.of(mockResponseDto), testPageable, 1);
	}

	@AfterEach
	void tearDown() throws Exception {
		mockResponseDto = null;
		mockPage = null;
	}

	@Test
	@DisplayName("POST /checkout - Success (200 OK)")
	void testCheckoutFromCart_Success() throws Exception {
		when(orderService.checkoutFromCart(ArgumentMatchers.anyDouble())).thenReturn(mockResponseDto);

		mockMvc
			.perform(post("/api/orders/checkout")
					.param("amount", "1500.00"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(500L))
			.andExpect(jsonPath("$.payment_status").value("SUCCESS"));
	}

	@Test
	@DisplayName("POST /checkout - Fails due to Null Result")
	void testCheckoutFromCart_Fails_NullResult() throws Exception {
		when(orderService.checkoutFromCart(ArgumentMatchers.anyDouble())).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(post("/api/orders/checkout").param("amount", "1500.00"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("Checkout Failed"));
	}

	@Test
	@DisplayName("POST /checkout - Fails due to 'FAILED' Payment Status")
	void testCheckoutFromCart_Fails_FailedPayment() throws Exception {
		mockResponseDto.setPayment_status("FAILED");
		when(orderService.checkoutFromCart(ArgumentMatchers.anyDouble())).thenReturn(mockResponseDto);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(post("/api/orders/checkout").param("amount", "1500.00"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("Checkout Failed"));
	}

	@Test
	@DisplayName("GET / - Success (Returns Page of Orders)")
	void testGetAllOrdersOfUser_Success() throws Exception {
		when(orderService.getAllOrders(ArgumentMatchers.any(Pageable.class))).thenReturn(mockPage);

		mockMvc
			.perform(get("/api/orders/")
					.param("page", "0")
					.param("size", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].id").value(500L));
	}

	@Test
	@DisplayName("GET / - Fails if Cart is Empty (CustomException)")
	void testGetAllOrdersOfUser_Empty() throws Exception {
		when(orderService.getAllOrders(ArgumentMatchers.any(Pageable.class))).thenReturn(Page.empty());

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/api/orders/"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("haven't placed any orders"));
	}

	@Test
	@DisplayName("GET /history - Success (Returns Page of Order History)")
	void testGetOrdersHistoryOfCurrentUser_Success() throws Exception {
		when(orderService.getOrdersHistory(ArgumentMatchers.any(Pageable.class))).thenReturn(mockPage);

		mockMvc
			.perform(get("/api/orders/history")
					.param("page", "0")
					.param("size", "5"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].order_status").value("SHIPPED"));
	}

	@Test
	@DisplayName("GET /history - Fails if History is Empty (CustomException)")
	void testGetOrdersHistoryOfCurrentUser_Empty() throws Exception {
		when(orderService.getOrdersHistory(ArgumentMatchers.any(Pageable.class))).thenReturn(Page.empty());

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/api/orders/history"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("haven't placed any orders"));
	}

	@Test
	@DisplayName("GET /{id} - Success (200 OK)")
	void testGetSpecificOrderDetails_Success() throws Exception {
		when(orderService.getOrderDetailsById(ArgumentMatchers.any(OrdersRequestDto.class))).thenReturn(mockResponseDto);

		mockMvc
			.perform(get("/api/orders/500"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(500L));
	}

	@Test
	@DisplayName("GET /{id} - Fails if Order Not Found (CustomException)")
	void testGetSpecificOrderDetails_NotFound() throws Exception {
		when(orderService.getOrderDetailsById(ArgumentMatchers.any(OrdersRequestDto.class))).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/api/orders/999"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("not found"));
	}

	@Test
	@DisplayName("PUT /{id}/status - Success (200 OK)")
	void testUpdateOrderStatusOnlyByAdmin_Success() throws Exception {
		mockResponseDto.setOrder_status("DELIVERED");
		when(orderService.updateOrderStatusById(ArgumentMatchers.any(OrdersRequestDto.class))).thenReturn(mockResponseDto);

		mockMvc
			.perform(put("/api/orders/500/status")
					.param("status", "DELIVERED"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.order_status").value("DELIVERED"));
	}

	@Test
	@DisplayName("PUT /{id}/status - Fails if Order Not Found (CustomException)")
	void testUpdateOrderStatusOnlyByAdmin_Fails() throws Exception {
		when(orderService.updateOrderStatusById(ArgumentMatchers.any(OrdersRequestDto.class))).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(put("/api/orders/500/status").param("status", "DELIVERED"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("Unable to update Status"));
	}

}
