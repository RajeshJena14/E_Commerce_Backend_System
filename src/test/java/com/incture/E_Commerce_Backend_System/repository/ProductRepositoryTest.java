package com.incture.E_Commerce_Backend_System.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.incture.E_Commerce_Backend_System.entity.Product;

@DataJpaTest
class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Product Repository...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Product Repository...");
	}

	@BeforeEach
	void setUp() throws Exception {
		Product p1 = new Product();
		p1.setName("Watch1");
		p1.setCategory("A");
		p1.setPrice(5000.0);
		p1.setRating(5);
		p1.setImage_url("example1.com");
		p1.setDescription("Good Watch1");
		p1.setStock(10);

		Product p2 = new Product();
		p2.setName("Watch2");
		p2.setCategory("A");
		p2.setPrice(50.0);
		p2.setRating(4);
		p2.setImage_url("example2.com");
		p2.setDescription("Good Watch2");
		p2.setStock(50);

		Product p3 = new Product();
		p3.setName("Watch3");
		p3.setCategory("B");
		p3.setPrice(1500.0);
		p3.setRating(5);
		p3.setImage_url("example3.com");
		p3.setDescription("Good Watch3");
		p3.setStock(15);

		Product p4 = new Product();
		p4.setName("Watch4");
		p4.setCategory("B");
		p4.setPrice(1200.0);
		p4.setRating(4);
		p4.setImage_url("example4.com");
		p4.setDescription("Good Watch4");
		p4.setStock(20);

		Product p5 = new Product();
		p5.setName("Watch5");
		p5.setCategory("B");
		p5.setPrice(300.0);
		p5.setRating(4);
		p5.setImage_url("example5.com");
		p5.setDescription("Good Watch5");
		p5.setStock(100);

		productRepository.saveAll(List.of(p1, p2, p3, p4, p5));
	}

	@AfterEach
	void tearDown() throws Exception {
		productRepository.deleteAll();
	}

	@DisplayName("Test for finding single product by precise Name")
	@Test
	void testFindByName() {
		Product foundProduct = productRepository.findByName("Watch2");
		Product missingProduct = productRepository.findByName("Watch100");

		assertNotNull(foundProduct, "Product should exist in the database");
		assertNull(missingProduct, "Unknown item should return null");
	}

	@DisplayName("Test for finding multiple product by precise Name")
	@ParameterizedTest
//	@CsvSource({ "Watch4", "Watch6" })
	@CsvSource({ "Watch1", "Watch3", "Watch5" })
	void testFindByName_Parametrized(String name) {
		Product foundProduct = productRepository.findByName(name);
		assertNotNull(foundProduct, "Product does not exist in the database");
	}

	@DisplayName("Test for finding products by single Category with Pagination")
	@Test
	void testFindByCategory() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Product> page1 = productRepository.findByCategory("A", pageable);
		Page<Product> page2 = productRepository.findByCategory("B", pageable);

		assertNotNull(page1);
		assertEquals(2, page1.getTotalElements(), "Should find exactly 2 A items");
		assertNotNull(page2);
		assertEquals(3, page2.getTotalElements(), "Should find exactly 3 B items");
	}

	@DisplayName("Test for finding products by multiple Category with Pagination")
	@ParameterizedTest
//	@CsvSource({ "A", "B", "C" })
	@CsvSource({ "A", "B" })
	void testFindByCategory_Parametrized(String category) {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Product> page = productRepository.findByCategory(category, pageable);

		assertNotNull(page);
//		assertTrue(page.getTotalElements() > 0, "Should have atleast 1 " + category + " item");
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
	}

	@DisplayName("Test for sorting products by Price (Ascending - Cheapest First)")
	@Test
	void testFindByOrderByPriceAsc() {
		List<Product> sortedProducts = productRepository.findByOrderByPriceAsc();
		assertEquals(5, sortedProducts.size(), "Should fetch all 5 products");
		assertEquals("Watch2", sortedProducts.get(0).getName());	// Watch2 - Rs 50
		assertEquals("Watch1", sortedProducts.get(4).getName());	// Watch1 - Rs 5000
	}

	@DisplayName("Test for sorting products by Price (Descending - Most Expensive First)")
	@Test
	void testFindByOrderByPriceDesc() {
		List<Product> sortedProducts = productRepository.findByOrderByPriceDesc();
		assertEquals(5, sortedProducts.size(), "Should fetch all 5 products");
		assertEquals("Watch1", sortedProducts.get(0).getName());	// Watch1 - Rs 5000
		assertEquals("Watch2", sortedProducts.get(4).getName());	// Watch2 - Rs 50
	}

}
