package com.incture.E_Commerce_Backend_System.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.incture.E_Commerce_Backend_System.entity.OrderItems;
import com.incture.E_Commerce_Backend_System.entity.Orders;
import com.incture.E_Commerce_Backend_System.entity.Product;
import com.incture.E_Commerce_Backend_System.entity.User;

@DataJpaTest
class OrdersRepositoryTest {

	@Autowired
	private OrdersRepository ordersRepository;

	@Autowired
	private OrderItemsRepository orderItemsRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	private User testUser1;
	private Product testProduct1;
	private Product testProduct2;
	private Product testProduct3;
	private LocalDateTime testDate;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Orders Repository...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Orders Repository...");
	}

	@BeforeEach
	void setUp() throws Exception {
		testUser1 = new User();
		testUser1.setName("ABC");
		testUser1.setEmail("abc@gmail.com");
		testUser1.setPassword("securePassword");
		testUser1.setRole("CUSTOMER");

		testUser1 = userRepository.save(testUser1);

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

		testProduct3 = new Product();
		testProduct3.setName("Watch3");
		testProduct3.setCategory("A");
		testProduct3.setPrice(999.0);
		testProduct3.setRating(5);
		testProduct3.setImage_url("example3.com");
		testProduct3.setDescription("Good Watch3");
		testProduct3.setStock(8);

		List<Product> savedList = productRepository.saveAll(List.of(testProduct1, testProduct2, testProduct3));
		testProduct1 = savedList.get(0);
		testProduct2 = savedList.get(1);
		testProduct3 = savedList.get(2);

		testDate = LocalDateTime.of(2026, 3, 14, 14, 27, 30);

		Orders order1 = new Orders();
		order1.setUser(testUser1);
		order1.setOrderDate(testDate);
		order1.setOrder_status("PLACED");
		order1.setPayment_status("SUCCESSFUL");

		OrderItems item1 = new OrderItems(order1, testProduct1, 2, testProduct1.getPrice() * 2);
		OrderItems item2 = new OrderItems(order1, testProduct2, 3, testProduct2.getPrice() * 3);
		order1.getOrderItems().addAll(List.of(item1, item2));

		order1.setTotal_amount(order1.getOrderItems().get(0).getPrice() + order1.getOrderItems().get(1).getPrice());

		Orders order2 = new Orders();
		order2.setUser(testUser1);
		order2.setOrderDate(testDate.minusDays(2));
		order2.setOrder_status("SHIPPED");
		order2.setPayment_status("SUCCESSFUL");

		OrderItems item3 = new OrderItems(order2, testProduct3, 1, testProduct3.getPrice() * 1);
		order2.getOrderItems().add(item3);

		order2.setTotal_amount(order2.getOrderItems().get(0).getPrice());

		ordersRepository.saveAll(List.of(order1, order2));

	}

	@AfterEach
	void tearDown() throws Exception {
		orderItemsRepository.deleteAll();
		ordersRepository.deleteAll();
		productRepository.deleteAll();
		userRepository.deleteAll();
	}

	@DisplayName("Test for fetching Orders by exact Order Date")
	@Test
	void testFindByOrderDate() {
		List<Orders> foundOrders = ordersRepository.findByOrderDate(testDate);

		assertNotNull(foundOrders, "Should have atleast one Order found on date: " + testDate);
		assertEquals(1, foundOrders.size(),
				"Should only find the one order placed at the exact order date: " + testDate);
	}

	@DisplayName("Test for fetching List of Orders by User ID (With JOIN FETCH)")
	@Test
	void testFindByUserIdLong() {
		List<Orders> userOrders = ordersRepository.findByUserId(testUser1.getId());

		assertNotNull(userOrders, "Should have atleast one Order found with User ID: " + testUser1.getId());
		assertEquals(2, userOrders.size(), "User: " + testUser1.getId() + " should have exactly 2 orders");

		// Test for JOIN FETCH working!
		assertFalse(userOrders.get(0).getOrderItems().isEmpty(), "Should have 2 order items but found 0");
		assertFalse(userOrders.get(1).getOrderItems().isEmpty(), "Should have 1 order item but found 0");

		assertEquals("Watch1", userOrders.get(0).getOrderItems().get(0).getProduct().getName());
		assertEquals("Watch2", userOrders.get(0).getOrderItems().get(1).getProduct().getName());
		assertEquals("Watch3", userOrders.get(1).getOrderItems().get(0).getProduct().getName());
	}

	@DisplayName("Test for fetching Page of Orders by User ID")
	@Test
	void testFindByUserIdLongPageable() {
		Pageable pageable = PageRequest.of(0, 5);
		Page<Orders> orderPage = ordersRepository.findByUserId(testUser1.getId(), pageable);

		assertNotNull(orderPage, "Should have atleast one Order found with User ID: " + testUser1.getId());
		assertEquals(2, orderPage.getTotalElements(),
				"Total elements across all pages is expected as 2 but is: " + orderPage.getTotalElements());
		assertEquals(1, orderPage.getTotalPages(), "Total pages is expected 1 but is: " + orderPage.getTotalPages());

		assertFalse(orderPage.getContent().get(0).getOrderItems().isEmpty(), "Should have 2 order items but found 0");
		assertFalse(orderPage.getContent().get(1).getOrderItems().isEmpty(), "Should have 1 order item but found 0");

		assertEquals("Watch1", orderPage.getContent().get(0).getOrderItems().get(0).getProduct().getName());
		assertEquals("Watch2", orderPage.getContent().get(0).getOrderItems().get(1).getProduct().getName());
		assertEquals("Watch3", orderPage.getContent().get(1).getOrderItems().get(0).getProduct().getName());
	}

}
