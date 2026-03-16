package com.incture.E_Commerce_Backend_System.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
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
class CartRepositoryTest {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Cart Repository...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Cart Repository...");
	}

	private User testUser1;
	private User testUser2;

	@BeforeEach
	void setUp() throws Exception {
		// For cart, user is needed
		User user1 = new User();
		user1.setName("User");
		user1.setEmail("user@cart.com");
		user1.setPassword("user1234");
		user1.setRole("CUSTOMER");

		User user2 = new User();
		user2.setName("Owner");
		user2.setEmail("owner@cart.com");
		user2.setPassword("owner9876");
		user2.setRole("ADMIN");

		List<User> savedUsers = userRepository.saveAll(List.of(user1, user2));
		testUser1 = savedUsers.get(0);
		testUser2 = savedUsers.get(1);

		// For cart, product is needed
		Product product1 = new Product();
		product1.setName("Product1");
		product1.setCategory("A");
		product1.setPrice(100.0);
		product1.setStock(50);
		product1.setImage_url("example1.com");
		product1.setDescription("Good product1");
		product1.setRating(5);

		Product product2 = new Product();
		product2.setName("Product2");
		product2.setCategory("B");
		product2.setPrice(1000.0);
		product2.setStock(25);
		product2.setImage_url("example2.com");
		product2.setDescription("Good product2");
		product2.setRating(4);

		Product product3 = new Product();
		product3.setName("Product3");
		product3.setCategory("A");
		product3.setPrice(599.99);
		product3.setStock(13);
		product3.setImage_url("example3.com");
		product3.setDescription("Good product3");
		product3.setRating(5);

		List<Product> savedProducts = productRepository.saveAll(List.of(product1, product2, product3));
		Product testProduct1 = savedProducts.get(0);
		Product testProduct2 = savedProducts.get(1);
		Product testProduct3 = savedProducts.get(2);

		// Creating Cart
		Cart testCart1 = new Cart();
		CartItem item1 = new CartItem();
		CartItem item2 = new CartItem();
		testCart1.setCartItems(new ArrayList<>());

		item1.setProduct(testProduct1);
		item1.setQuantity(2);
		item1.setCart(testCart1);

		item2.setProduct(testProduct2);
		item2.setQuantity(3);
		item2.setCart(testCart1);

		testCart1.setUser(testUser1);
//		testUser1.setCart(testCart1);
		testCart1.setTotal_price(item1.getProduct().getPrice() * item1.getQuantity()
				+ item2.getProduct().getPrice() * item2.getQuantity());

		testCart1.getCartItems().addAll(List.of(item1, item2));

		Cart testCart2 = new Cart();
		CartItem item3 = new CartItem();
		testCart2.setCartItems(new ArrayList<>());

		item3.setProduct(testProduct3);
		item3.setQuantity(5);
		item3.setCart(testCart2);

		testCart2.setUser(testUser2);
//		testUser2.setCart(testCart2);
		testCart2.setTotal_price(item3.getProduct().getPrice() * item3.getQuantity());

		testCart2.getCartItems().add(item3);

		cartRepository.saveAll(List.of(testCart1, testCart2));
	}

	@AfterEach
	void tearDown() throws Exception {
		cartRepository.deleteAll();
		productRepository.deleteAll();
		userRepository.deleteAll();
	}

	@DisplayName("Test for fetching Cart by User ID (Includes JOIN FETCH verification)")
	@Test
	void testFindByUserId() {
		Cart fetchedCart1 = cartRepository.findByUserId(testUser1.getId());
		Cart fetchedCart2 = cartRepository.findByUserId(testUser2.getId());

		long nonExistentUserId = 1024L;
		Cart missingCart = cartRepository.findByUserId(nonExistentUserId);
		assertNull(missingCart, "Should return null for the non-existent user ID: " + nonExistentUserId);

		assertNotNull(fetchedCart1, "Cart does not exist for the given user");
		assertNotNull(fetchedCart2, "Cart does not exist for the given user");

		assertEquals(testUser1.getId(), fetchedCart1.getUser().getId(), "Actual User ID: "
				+ fetchedCart1.getUser().getId() + "did not match with expected Usert ID: " + testUser1.getId());
		assertEquals(testUser2.getId(), fetchedCart2.getUser().getId(), "Actual User ID: "
				+ fetchedCart2.getUser().getId() + "did not match with expected Usert ID: " + testUser2.getId());

		assertEquals(3200.0, fetchedCart1.getTotal_price());
		assertEquals(2999.95, fetchedCart2.getTotal_price());

		// LEFT JOIN FETCH verification...
		assertFalse(fetchedCart1.getCartItems().isEmpty(),
				"Cart items did not get fetched alongside the cart ID: " + fetchedCart1.getId());
		assertFalse(fetchedCart2.getCartItems().isEmpty(),
				"Cart items did not get fetched alongside the cart ID: " + fetchedCart2.getId());

		assertEquals(2, fetchedCart1.getCartItems().size(),
				"Expected size did not match Actual size for cart ID: " + fetchedCart1.getId());
		assertEquals(1, fetchedCart2.getCartItems().size(),
				"Expected size did not match Actual size for cart ID: " + fetchedCart2.getId());
	}

}
