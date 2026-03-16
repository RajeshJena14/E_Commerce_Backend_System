package com.incture.E_Commerce_Backend_System.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.incture.E_Commerce_Backend_System.dto.CartItemRequestDto;
import com.incture.E_Commerce_Backend_System.dto.CartResponseDto;
import com.incture.E_Commerce_Backend_System.dto.UserResponseDto;
import com.incture.E_Commerce_Backend_System.entity.Cart;
import com.incture.E_Commerce_Backend_System.entity.CartItem;
import com.incture.E_Commerce_Backend_System.entity.Product;
import com.incture.E_Commerce_Backend_System.entity.User;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.repository.CartItemRepository;
import com.incture.E_Commerce_Backend_System.repository.CartRepository;
import com.incture.E_Commerce_Backend_System.repository.ProductRepository;
import com.incture.E_Commerce_Backend_System.repository.UserRepository;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private CartRepository cartRepository;

	@Mock
	private CartItemRepository cartItemRepository;

	@InjectMocks
	private CartService cartService;

	@Spy
	private ModelMapper modelMapper = new ModelMapper();

	private User mockUser;
	private UserResponseDto mockLoggedInUser; // Imitating Logged-In User
	private Product mockProduct;
	private Cart mockCart;
	private CartItem mockCartItem;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Cart Service...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Cart Service...");
	}

	@BeforeEach
	void setUp() throws Exception {
		mockUser = new User();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		mockUser.setName("CartTester");
		mockUser.setRole("CUSTOMER");

		// Imitating Logged-In User
		mockLoggedInUser = new UserResponseDto();
		mockLoggedInUser.setId(1L);
		mockLoggedInUser.setName("CartTester");
		mockLoggedInUser.setRole("CUSTOMER");

		mockProduct = new Product();
		ReflectionTestUtils.setField(mockProduct, "id", 100L);
		mockProduct.setName("Test Watch");
		mockProduct.setPrice(500.0);
		mockProduct.setStock(10);
		mockProduct.setCategory("Watches");
		mockProduct.setDescription("Good Watch...");

		mockCart = new Cart();
		ReflectionTestUtils.setField(mockCart, "id", 50L);
		mockCart.setUser(mockUser);
		mockCart.setTotal_price(500.0);
		mockCart.setCartItems(new ArrayList<>());

		mockCartItem = new CartItem(mockCart, mockProduct, 1);
		ReflectionTestUtils.setField(mockCartItem, "id", 500L);
		mockCart.getCartItems().add(mockCartItem);
	}

	@AfterEach
	void tearDown() throws Exception {
		SecurityContextHolder.clearContext();
		mockUser = null;
		mockLoggedInUser = null;
		mockProduct = null;
		mockCart = null;
		mockCartItem = null;
	}

	private void mockSecurityContext(UserResponseDto loggedInUser) {
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication authentication = mock(Authentication.class);

		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(loggedInUser);

		SecurityContextHolder.setContext(securityContext);
	}

	@DisplayName("Test for Adding Item to Cart - Success (New item in existing cart)")
	@Test
	void testAddItemToCart_Success() {
		mockSecurityContext(mockLoggedInUser);

		CartItemRequestDto requestDto = new CartItemRequestDto();
		requestDto.setProduct_id(100L);
		requestDto.setQuantity(2);

		when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));
		when(cartItemRepository.findByCartAndProduct(mockCart, mockProduct)).thenReturn(null);

		when(cartRepository.save(ArgumentMatchers.any(Cart.class)))
				.thenAnswer(passedArguments -> passedArguments.getArgument(0));

		CartResponseDto result = cartService.addItemToCart(requestDto);

		assertNotNull(result);
		assertEquals(1500.0, result.getTotal_price());
		verify(cartRepository, times(1)).save(ArgumentMatchers.any(Cart.class));
	}

	@DisplayName("Test for Adding Item to Cart - Fails due to Insufficient Stock")
	@ParameterizedTest
	@CsvSource({ "11", "25", "100" })
	void testAddItemToCart_InsufficientStock(int quantity) {
		mockSecurityContext(mockLoggedInUser);

		CartItemRequestDto requestDto = new CartItemRequestDto();
		requestDto.setProduct_id(100L);
		requestDto.setQuantity(quantity);

		when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));
		when(cartItemRepository.findByCartAndProduct(mockCart, mockProduct)).thenReturn(null);

		assertThrows(CustomException.class, () -> cartService.addItemToCart(requestDto));

		verify(cartRepository, never()).save(ArgumentMatchers.any(Cart.class));
	}

	@DisplayName("Test for Adding Item - Fails due to Invalid Session (No Auth)")
	@Test
	void testAddItemToCart_AuthFailure() {
		CartItemRequestDto requestDto = new CartItemRequestDto();
		requestDto.setProduct_id(100L);
		requestDto.setQuantity(1);

		assertThrows(AuthenticationCredentialsNotFoundException.class, () -> cartService.addItemToCart(requestDto));

		verify(userRepository, never()).findById(ArgumentMatchers.anyLong());
	}

	@DisplayName("Test for Adding Item - Fails because User is not in Database")
	@Test
	void testAddItemToCart_UserNotFound() {
		mockSecurityContext(mockLoggedInUser);

		CartItemRequestDto requestDto = new CartItemRequestDto();
		requestDto.setProduct_id(100L);
		requestDto.setQuantity(1);

		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> cartService.addItemToCart(requestDto));
	}

	@DisplayName("Test for Adding Item - Returns null because Product does not exist")
	@Test
	void testAddItemToCart_ProductNotFound() {
		mockSecurityContext(mockLoggedInUser);

		CartItemRequestDto requestDto = new CartItemRequestDto();
		requestDto.setProduct_id(999L);

		when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		CartResponseDto result = cartService.addItemToCart(requestDto);

		assertNull(result);
	}

	@DisplayName("Test for Adding Item - Fails because Product is completely Out of Stock")
	@Test
	void testAddItemToCart_ZeroStock() {
		mockSecurityContext(mockLoggedInUser);

		CartItemRequestDto requestDto = new CartItemRequestDto();
		requestDto.setProduct_id(100L);
		requestDto.setQuantity(1);

		mockProduct.setStock(0);

		when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));

		CustomException exception = assertThrows(CustomException.class, () -> cartService.addItemToCart(requestDto));

		assertTrue(exception.getMessage().contains("out of stock"));
	}

	@DisplayName("Test for Adding Item - Fails to increase quantity of existing item due to stock limits")
	@Test
	void testAddItemToCart_InsufficientStockForExistingItem() {
		mockSecurityContext(mockLoggedInUser);

		CartItemRequestDto requestDto = new CartItemRequestDto();
		requestDto.setProduct_id(100L);
		requestDto.setQuantity(10); // adding extra 10 units

		when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(productRepository.findById(100L)).thenReturn(Optional.of(mockProduct));

		when(cartItemRepository.findByCartAndProduct(mockCart, mockProduct)).thenReturn(mockCartItem);

		assertThrows(CustomException.class, () -> cartService.addItemToCart(requestDto));

		verify(cartRepository, never()).save(ArgumentMatchers.any(Cart.class));
	}

	@DisplayName("Test for Showing Cart - Success")
	@Test
	void testShowCart_Success() {
		mockSecurityContext(mockLoggedInUser);

		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);

		CartResponseDto result = cartService.showCart();

		assertNotNull(result);
		assertEquals(500.0, result.getTotal_price());
		assertEquals(1, result.getCartItems().size());
	}

	@DisplayName("Test for Showing Cart - Empty Cart returns null")
	@Test
	void testShowCart_EmptyCart() {
		mockSecurityContext(mockLoggedInUser);

		Cart emptyCart = new Cart();
		ReflectionTestUtils.setField(emptyCart, "id", 51L);
		emptyCart.setCartItems(new ArrayList<>());

		when(cartRepository.findByUserId(1L)).thenReturn(emptyCart);

		CartResponseDto result = cartService.showCart();

		assertNull(result, "CartService should return null if cart = empty");
	}

	@DisplayName("Test for Showing Total Price - Success")
	@Test
	void testShowTotalPrice_Success() {
		mockSecurityContext(mockLoggedInUser);
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);

		CartResponseDto result = cartService.showTotalPrice();

		assertNotNull(result);
		assertEquals(500.0, result.getTotal_price());
	}

	@DisplayName("Test for Showing Total Price - Returns null for Empty Cart")
	@Test
	void testShowTotalPrice_EmptyCart() {
		mockSecurityContext(mockLoggedInUser);

		Cart emptyCart = new Cart();
		ReflectionTestUtils.setField(emptyCart, "id", 51L);
		emptyCart.setCartItems(new ArrayList<>());

		when(cartRepository.findByUserId(1L)).thenReturn(emptyCart);

		CartResponseDto result = cartService.showTotalPrice();

		assertNull(result);
	}

	@DisplayName("Test for Deleting Item from Cart - Success")
	@Test
	void testDeleteItemFromCart_Success() {
		mockSecurityContext(mockLoggedInUser);

		CartItemRequestDto deleteDto = new CartItemRequestDto();
		deleteDto.setProduct_id(100L);

		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(cartRepository.save(ArgumentMatchers.any(Cart.class)))
				.thenAnswer(passedArguments -> passedArguments.getArgument(0));

		CartResponseDto result = cartService.deleteItemFromCart(deleteDto);

		assertNotNull(result);
		assertEquals(0.0, result.getTotal_price());
		assertTrue(result.getCartItems().isEmpty());
	}

	@DisplayName("Test for Deleting Item - Fails because Cart is Empty")
	@Test
	void testDeleteItemFromCart_EmptyCart() {
		mockSecurityContext(mockLoggedInUser);

		Cart emptyCart = new Cart();
		emptyCart.setCartItems(new ArrayList<>());
		when(cartRepository.findByUserId(1L)).thenReturn(emptyCart);

		CartItemRequestDto deleteDto = new CartItemRequestDto();
		deleteDto.setProduct_id(100L);

		CustomException exception = assertThrows(CustomException.class,
				() -> cartService.deleteItemFromCart(deleteDto));
		assertTrue(exception.getMessage().contains("cart is currently empty"));
	}

	@DisplayName("Test for Deleting Item - Fails because Item is not in Cart")
	@Test
	void testDeleteItemFromCart_ItemNotFound() {
		mockSecurityContext(mockLoggedInUser);
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);

		CartItemRequestDto deleteDto = new CartItemRequestDto();
		deleteDto.setProduct_id(999L);

		assertThrows(NoSuchElementException.class, () -> cartService.deleteItemFromCart(deleteDto));
	}

	@DisplayName("Test for Updating Quantity in Cart - Success")
	@Test
	void testUpdateQuantityInCart_Success() {
		mockSecurityContext(mockLoggedInUser);

		CartItemRequestDto updateDto = new CartItemRequestDto();
		updateDto.setProduct_id(100L);
		updateDto.setQuantity(3); // Updated

		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);
		when(cartRepository.save(ArgumentMatchers.any(Cart.class)))
				.thenAnswer(passedArguments -> passedArguments.getArgument(0));

		CartResponseDto result = cartService.updateQuantityInCart(updateDto);

		assertNotNull(result);
		assertEquals(3, result.getCartItems().get(0).getQuantity());
		assertEquals(1500.0, result.getTotal_price());
	}

	@DisplayName("Test for Updating Quantity - Returns null because Cart is Empty")
	@Test
	void testUpdateQuantityInCart_EmptyCart() {
		mockSecurityContext(mockLoggedInUser);

		Cart emptyCart = new Cart();
		emptyCart.setCartItems(new ArrayList<>());
		when(cartRepository.findByUserId(1L)).thenReturn(emptyCart);

		CartItemRequestDto updateDto = new CartItemRequestDto();
		updateDto.setProduct_id(100L);
		updateDto.setQuantity(5);

		CartResponseDto result = cartService.updateQuantityInCart(updateDto);

		assertNull(result);
	}

	@DisplayName("Test for Updating Quantity - Fails because Item is not in Cart")
	@Test
	void testUpdateQuantityInCart_ItemNotFound() {
		mockSecurityContext(mockLoggedInUser);
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);

		CartItemRequestDto updateDto = new CartItemRequestDto();
		updateDto.setProduct_id(999L);
		updateDto.setQuantity(5);

		assertThrows(NoSuchElementException.class, () -> cartService.updateQuantityInCart(updateDto));
	}

	@DisplayName("Test for Updating Quantity - Fails due to Insufficient Stock")
	@Test
	void testUpdateQuantityInCart_InsufficientStock() {
		mockSecurityContext(mockLoggedInUser);
		when(cartRepository.findByUserId(1L)).thenReturn(mockCart);

		CartItemRequestDto updateDto = new CartItemRequestDto();
		updateDto.setProduct_id(100L); // original: 10 stock
		updateDto.setQuantity(50); // quantity required: 50

		CustomException exception = assertThrows(CustomException.class,
				() -> cartService.updateQuantityInCart(updateDto));
		assertTrue(exception.getMessage().contains("Insufficient stock"));

		verify(cartRepository, never()).save(ArgumentMatchers.any(Cart.class));
	}

}
