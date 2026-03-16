package com.incture.E_Commerce_Backend_System.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.incture.E_Commerce_Backend_System.dto.CartItemRequestDto;
import com.incture.E_Commerce_Backend_System.dto.CartResponseDto;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.service.CartService;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

	private MockMvc mockMvc;

	@Mock
	private CartService cartService;

	@InjectMocks
	private CartController cartController;

	private CartResponseDto mockResponseDto;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Cart Controller...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Cart Controller...");
	}

	@BeforeEach
	void setUp() throws Exception {
		mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();

		mockResponseDto = new CartResponseDto();
		mockResponseDto.setTotal_price(2500.00);
		mockResponseDto.setCartItems(List.of());
	}

	@AfterEach
	void tearDown() throws Exception {
		mockResponseDto = null;
	}

	@Test
	@DisplayName("POST /add/{productId} - Success (201 Created)")
	void testAddProductToCart_Success() throws Exception {
		when(cartService.addItemToCart(ArgumentMatchers.any(CartItemRequestDto.class))).thenReturn(mockResponseDto);

		mockMvc
			.perform(post("/api/cart/add/10")
				.param("quantity", "2"))
			.andExpect(status().isCreated())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Product added to cart")));
	}

	@Test
	@DisplayName("POST /add/{productId} - Fails (Internal Server Error)")
	void testAddProductToCart_Fails() throws Exception {
		when(cartService.addItemToCart(ArgumentMatchers.any(CartItemRequestDto.class))).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(post("/api/cart/add/10").param("quantity", "2"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("Unable to add the product"));
	}

	@Test
	@DisplayName("GET / - Success (200 OK)")
	void testShowCart_Success() throws Exception {
		when(cartService.showCart()).thenReturn(mockResponseDto);

		mockMvc
			.perform(get("/api/cart/"))
			.andExpect(status().isOk())
			.andExpect(content().string(Matchers.containsString("Your Cart:")));
	}

	@Test
	@DisplayName("GET / - Fails if Cart Empty (Not Found Error)")
	void testShowCart_Empty() throws Exception {
		when(cartService.showCart()).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/api/cart/"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("Your cart is currently empty"));
	}

	@Test
	@DisplayName("GET /showPrice - Success (200 OK)")
	void testShowTotalPrice_Success() throws Exception {
		when(cartService.showTotalPrice()).thenReturn(mockResponseDto);

		mockMvc
			.perform(get("/api/cart/showPrice"))
			.andExpect(status().isOk())
			.andExpect(content().string(Matchers.containsString("Your Breakdown:")));
	}

	@Test
	@DisplayName("GET /showPrice - Fails if Cart Empty (Not Found Error)")
	void testShowTotalPrice_Empty() throws Exception {
		when(cartService.showTotalPrice()).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/api/cart/showPrice"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("No items found in your cart"));
	}

	@Test
	@DisplayName("DELETE /remove/{productId} - Success (200 OK)")
	void testDeleteProductFromCart_Success() throws Exception {
		when(cartService.deleteItemFromCart(ArgumentMatchers.any(CartItemRequestDto.class))).thenReturn(mockResponseDto);

		mockMvc
			.perform(delete("/api/cart/remove/10"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("DELETE /remove/{productId} - Fails (Internal Server Error)")
	void testDeleteProductFromCart_Fails() throws Exception {
		when(cartService.deleteItemFromCart(ArgumentMatchers.any(CartItemRequestDto.class))).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(delete("/api/cart/remove/10"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("Unable to remove item"));
	}

	@Test
	@DisplayName("PUT /update/{productId} - Success (200 OK)")
	void testUpdateProductQuantityToCart_Success() throws Exception {
		when(cartService.updateQuantityInCart(ArgumentMatchers.any(CartItemRequestDto.class))).thenReturn(mockResponseDto);

		mockMvc
			.perform(put("/api/cart/update/10")
				.param("quantity", "5"))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Product added to cart")));
	}

	@Test
	@DisplayName("PUT /update/{productId} - Fails (Internal Server Error)")
	void testUpdateProductQuantityToCart_Fails() throws Exception {
		when(cartService.updateQuantityInCart(ArgumentMatchers.any(CartItemRequestDto.class))).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(put("/api/cart/update/10").param("quantity", "5"));
		});

		assertTrue(exception.getCause() instanceof CustomException);
		assertTrue(exception.getCause().getMessage().contains("Unable to update quantity"));
	}

}
