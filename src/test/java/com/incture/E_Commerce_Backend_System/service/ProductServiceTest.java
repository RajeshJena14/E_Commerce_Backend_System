package com.incture.E_Commerce_Backend_System.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.incture.E_Commerce_Backend_System.dto.ProductIdRequestDTO;
import com.incture.E_Commerce_Backend_System.dto.ProductRequestDTO;
import com.incture.E_Commerce_Backend_System.dto.ProductResponseDTO;
import com.incture.E_Commerce_Backend_System.entity.Product;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	@Spy
	private ModelMapper modelMapper = new ModelMapper();

	private Product mockProduct1;

	private Product mockProduct2;

	private Product mockProduct3;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Product Service...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Product Service...");
	}

	@BeforeEach
	void setUp() throws Exception {
		mockProduct1 = new Product();
		ReflectionTestUtils.setField(mockProduct1, "id", 101L);
		mockProduct1.setName("TimelessTicks Chronograph");
		mockProduct1.setCategory("Luxury Watches");
		mockProduct1.setPrice(450.0);
		mockProduct1.setStock(20);
		mockProduct1.setDescription("A premium stainless steel watch.");

		mockProduct2 = new Product();
		ReflectionTestUtils.setField(mockProduct2, "id", 202L);
		mockProduct2.setName("Rolex Midnight");
		mockProduct2.setCategory("Luxury Watches");
		mockProduct2.setPrice(650.0);
		mockProduct2.setStock(17);
		mockProduct2.setDescription("A premium stainless steel watch.");

		mockProduct3 = new Product();
		ReflectionTestUtils.setField(mockProduct3, "id", 303L);
		mockProduct3.setName("Onyx Roadster");
		mockProduct3.setCategory("Racing Watches");
		mockProduct3.setPrice(1099.0);
		mockProduct3.setStock(30);
		mockProduct3.setDescription("Equipped with Carbon fibre.");
	}

	@AfterEach
	void tearDown() throws Exception {
		mockProduct1 = null;
		mockProduct2 = null;
		mockProduct3 = null;
	}

	@DisplayName("Test for fetching all products with pagination")
	@Test
	void testGetAllProducts() {
		Pageable pageable = PageRequest.of(0, 5);
		Page<Product> mockPage = new PageImpl<>(List.of(mockProduct1, mockProduct2, mockProduct3));

		when(productRepository.findAll(pageable)).thenReturn(mockPage);

		// Call the mock "productRepository" inside actual method "getAllProducts()"
		Page<ProductResponseDTO> result = productService.getAllProducts(pageable);

		assertNotNull(result);
		assertEquals(3, result.getTotalElements());
		assertEquals("TimelessTicks Chronograph", result.getContent().get(0).getName());
		assertEquals("Rolex Midnight", result.getContent().get(1).getName());
		assertEquals("Onyx Roadster", result.getContent().get(2).getName());
	}

	@DisplayName("Test for fetching products by category with pagination")
	@Test
	void testGetProductsByCategory() {
		Pageable pageable = PageRequest.of(0, 5);
		Page<Product> mockPage = new PageImpl<>(List.of(mockProduct1, mockProduct2));

		when(productRepository.findByCategory("Luxury Watches", pageable)).thenReturn(mockPage);

		Page<ProductResponseDTO> result1 = productService.getProductsByCategory("Luxury Watches", pageable);

		assertNotNull(result1);
		assertEquals(2, result1.getTotalElements()); // 2 elements in provided category
		assertEquals("Luxury Watches", result1.getContent().get(0).getCategory());
		assertEquals("Luxury Watches", result1.getContent().get(1).getCategory());
	}

	@DisplayName("Test for fetching specific product details - Success")
	@Test
	void testGetProductDetailsById_Success() {
		ProductIdRequestDTO requestDTO = new ProductIdRequestDTO();
		requestDTO.setId(303L);

		when(productRepository.findById(303L)).thenReturn(Optional.of(mockProduct3));

		ProductResponseDTO result = productService.getProductDetailsById(requestDTO);

		assertNotNull(result);
		assertEquals(303L, result.getId());
		assertEquals("Onyx Roadster", result.getName());
	}

	@DisplayName("Test for fetching specific product details - Not Found")
	@Test
	void testGetProductDetailsById_NotFound() {
		ProductIdRequestDTO requestDTO = new ProductIdRequestDTO();
		requestDTO.setId(999L); // ID that doesn't exist

		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		ProductResponseDTO result = productService.getProductDetailsById(requestDTO);

		assertNull(result, "Should return null when product is not found in database");
	}

	@DisplayName("Test for adding a new product - Success")
	@Test
	void testAddProduct() {
		ProductRequestDTO newProductReq = new ProductRequestDTO();
		newProductReq.setName("TimelessTicks Diver");
		newProductReq.setPrice(300.0);
		newProductReq.setCategory("Diver Watches");
		newProductReq.setStock(50);

		when(productRepository.findByName("TimelessTicks Diver")).thenReturn(null);

		Product savedProduct = new Product();
		ReflectionTestUtils.setField(savedProduct, "id", 102L);
		savedProduct.setName("TimelessTicks Diver");
		savedProduct.setPrice(300.0);
		savedProduct.setCategory("Diver Watches");
		savedProduct.setStock(50);

		when(productRepository.save(ArgumentMatchers.any(Product.class))).thenReturn(savedProduct);

		ProductResponseDTO result = productService.addProduct(newProductReq);

		assertNotNull(result);
		assertEquals("TimelessTicks Diver", result.getName());
		assertEquals(102L, result.getId());
		assertEquals("Diver Watches", result.getCategory());
		verify(productRepository, times(1)).save(ArgumentMatchers.any(Product.class));
	}

	@DisplayName("Test for adding a new product - Fails due to Duplicate Name")
	@Test
	void testAddProduct_DuplicateName() {
		ProductRequestDTO duplicateReq = new ProductRequestDTO();
		duplicateReq.setName("TimelessTicks Chronograph");

		when(productRepository.findByName("TimelessTicks Chronograph")).thenReturn(mockProduct1);

		assertThrows(CustomException.class, () -> productService.addProduct(duplicateReq));
		verify(productRepository, never()).save(ArgumentMatchers.any(Product.class));
	}

	@DisplayName("Test for deleting a product by Admin - Success")
	@Test
	void testDeleteProductByIdByAdmin() {
		ProductIdRequestDTO requestDTO = new ProductIdRequestDTO();
		requestDTO.setId(202L);

		when(productRepository.findById(202L)).thenReturn(Optional.of(mockProduct2));

		ProductResponseDTO result = productService.deleteProductByIdByAdmin(requestDTO);

		assertNotNull(result);
		verify(productRepository, times(1)).deleteById(202L);
	}

	@DisplayName("Test for updating a product - Success")
	@Test
	void testUpdateProductDetailsById() {
		ProductRequestDTO updateReq = new ProductRequestDTO();
		updateReq.setPrice(500.0);
		updateReq.setStock(13);

		when(productRepository.findById(303L)).thenReturn(Optional.of(mockProduct1));
		when(productRepository.save(ArgumentMatchers.any(Product.class))).thenAnswer(passedArguments -> passedArguments.getArgument(0));

		ProductResponseDTO result = productService.updateProductDetailsById(303L, updateReq);

		assertNotNull(result);
		assertEquals(500.0, result.getPrice());
		assertEquals("TimelessTicks Chronograph", result.getName());
		assertEquals(13, result.getStock());
	}

}
