package com.incture.E_Commerce_Backend_System.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.incture.E_Commerce_Backend_System.entity.Cart;
import com.incture.E_Commerce_Backend_System.entity.CartItem;
import com.incture.E_Commerce_Backend_System.entity.Product;
import com.incture.E_Commerce_Backend_System.entity.User;

@DataJpaTest
class CartItemRepositoryTest {

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	private User testUser1;
	private User testUser2;

	private Product testProduct1;
	private Product testProduct2;

	private Cart cart1;
	private Cart cart2;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Cart Item...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Cart Item...");
	}

	@BeforeEach
	void setUp() throws Exception {
		testUser1 = new User();
		testUser1.setName("ABC");
		testUser1.setEmail("abc@gmail.com");
		testUser1.setPassword("securePassword");
		testUser1.setRole("CUSTOMER");

		testUser2 = new User();
		testUser2.setName("XYZ");
		testUser2.setEmail("xyz@gmail.com");
		testUser2.setPassword("123456");
		testUser2.setRole("ADMIN");

		List<User> savedUsers = userRepository.saveAll(List.of(testUser1, testUser2));
		testUser1 = savedUsers.get(0);
		testUser2 = savedUsers.get(1);

		testProduct1 = new Product();
		testProduct1.setName("Watch1");
		testProduct1.setCategory("A");
		testProduct1.setPrice(5000.0);
		testProduct1.setRating(5);
		testProduct1.setImage_url("example1.com");
		testProduct1.setDescription("Good Watch1");
		testProduct1.setStock(10);

		testProduct2 = new Product();
		testProduct2.setName("Watch2");
		testProduct2.setCategory("B");
		testProduct2.setPrice(499.0);
		testProduct2.setRating(4);
		testProduct2.setImage_url("example2.com");
		testProduct2.setDescription("Good Watch2");
		testProduct2.setStock(50);

		List<Product> savedProducts = productRepository.saveAll(List.of(testProduct1, testProduct2));
		testProduct1 = savedProducts.get(0);
		testProduct2 = savedProducts.get(1);

		cart1 = new Cart();
		cart2 = new Cart();
		cart1.setUser(testUser1);
		cart2.setUser(testUser2);

	}

	@AfterEach
	void tearDown() throws Exception {
		cartItemRepository.deleteAll();
		cartRepository.deleteAll();
		userRepository.deleteAll();
		productRepository.deleteAll();
	}

	@DisplayName("Test for finding CartItem by exact Cart and Product (1 Cart -> Multiple Product)")
	@Test
	void testFindByCartAndProductSingleCartMultipleItems() {
		CartItem item1 = new CartItem();
		CartItem item2 = new CartItem();

		item1.setProduct(testProduct1);
		item1.setQuantity(3);
		item1.setCart(cart1);

		item2.setProduct(testProduct2);
		item2.setQuantity(5);
		item2.setCart(cart1);

		List<CartItem> savedItems = cartItemRepository.saveAll(List.of(item1, item2));
		item1 = savedItems.get(0);
		item2 = savedItems.get(1);

		cart1.getCartItems().addAll(List.of(item1, item2));
		cart1.setTotal_price(cart1.getCartItems().get(0).getProduct().getPrice()
				* cart1.getCartItems().get(0).getQuantity()
				+ cart1.getCartItems().get(1).getProduct().getPrice() * cart1.getCartItems().get(0).getQuantity());

		cart1 = cartRepository.save(cart1);

		CartItem result = cartItemRepository.findByCartAndProduct(cart1, testProduct1);
		assertNotNull(result, "CartItem should exist for this Cart ID: " + cart1.getId() + " and Product ID: "
				+ testProduct1.getId());
		assertEquals("Watch1", result.getProduct().getName());
	}

	@DisplayName("Test for finding CartItem by exact Cart and Product (Multiple Cart -> 1 Product)")
	@Test
	void testFindByCartAndProductMultipleCartsSingleItem() {
		CartItem item1 = new CartItem();
		CartItem item2 = new CartItem();

		item1.setProduct(testProduct1);
		item1.setQuantity(3);
		item1.setCart(cart1);

		item2.setProduct(testProduct1);
		item2.setQuantity(5);
		item2.setCart(cart2);

		List<CartItem> savedItems = cartItemRepository.saveAll(List.of(item1, item2));
		item1 = savedItems.get(0);
		item2 = savedItems.get(1);

		cart1.getCartItems().add(item1);
		cart2.getCartItems().add(item2);

		cart1.setTotal_price(
				cart1.getCartItems().get(0).getProduct().getPrice() * cart1.getCartItems().get(0).getQuantity());
		cart2.setTotal_price(
				cart2.getCartItems().get(0).getProduct().getPrice() * cart2.getCartItems().get(0).getQuantity());

		List<Cart> savedCarts = cartRepository.saveAll(List.of(cart1, cart2));
		cart1 = savedCarts.get(0);
		cart2 = savedCarts.get(1);

		CartItem result1 = cartItemRepository.findByCartAndProduct(cart1, testProduct1);
		CartItem result2 = cartItemRepository.findByCartAndProduct(cart2, testProduct1);

		assertNotNull(result1, "CartItem should exist for this Cart ID: " + cart1.getId() + " and Product ID: "
				+ testProduct1.getId());
		assertEquals("Watch1", result1.getProduct().getName());

		assertNotNull(result2, "CartItem should exist for this Cart ID: " + cart2.getId() + " and Product ID: "
				+ testProduct1.getId());
		assertEquals("Watch1", result2.getProduct().getName());
	}
}
