package com.incture.E_Commerce_Backend_System.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.NoSuchElementException;

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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incture.E_Commerce_Backend_System.dto.ProductIdRequestDTO;
import com.incture.E_Commerce_Backend_System.dto.ProductRequestDTO;
import com.incture.E_Commerce_Backend_System.dto.ProductResponseDTO;
import com.incture.E_Commerce_Backend_System.entity.Product;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@Mock
	private ProductService productService;

	@Spy
	private ModelMapper modelMapper = new ModelMapper();

	@InjectMocks
	private ProductController productController;

	private Product mockProduct;

	private ProductResponseDTO mockResponseDto;

	private Page<ProductResponseDTO> mockPage;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Product Controller...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Product Controller...");
	}

	@BeforeEach
	void setUp() throws Exception {
		mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
		objectMapper = new ObjectMapper();

		mockProduct = new Product();
		mockProduct.setName("Gaming Laptop");
		mockProduct.setDescription("High performance laptop");
		mockProduct.setCategory("Electronics");
		mockProduct.setPrice(1500.00);
		mockProduct.setStock(10);

		mockResponseDto = new ProductResponseDTO();
		mockResponseDto.setId(100L);
		mockResponseDto.setName("Gaming Laptop");
		mockResponseDto.setCategory("Electronics");
		mockResponseDto.setPrice(1500.00);
		mockResponseDto.setStock(10);

		Pageable testPageable = PageRequest.of(0, 5, Sort.by("id").ascending());
		mockPage = new PageImpl<>(List.of(mockResponseDto), testPageable, 1);
	}

	@AfterEach
	void tearDown() throws Exception {
		mockProduct = null;
		mockResponseDto = null;
		mockPage = null;
	}

	@Test
	@DisplayName("GET / - Success (Returns Page of Products)")
	void testGetAllProducts_Success() throws Exception {
		when(productService.getAllProducts(ArgumentMatchers.any(Pageable.class))).thenReturn(mockPage);

		mockMvc
			.perform(get("/api/products/")
				.param("page", "0")
				.param("size", "5")
				.param("sortBy", "id")
				.param("ascending", "true"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].name").value("Gaming Laptop"));
	}

	@Test
	@DisplayName("GET / - Fails if no products exist (NoSuchElementException)")
	void testGetAllProducts_EmptyList() throws Exception {
		when(productService.getAllProducts(ArgumentMatchers.any(Pageable.class))).thenReturn(Page.empty());

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/api/products/"));
		});

		assertTrue(exception.getCause() instanceof NoSuchElementException);
	}

	@Test
	@DisplayName("GET /category - Success (Returns Page of Products)")
	void testGetProductsByCategory_Success() throws Exception {
		when(productService.getProductsByCategory(ArgumentMatchers.anyString(), ArgumentMatchers.any(Pageable.class))).thenReturn(mockPage);

		mockMvc
			.perform(get("/api/products/category")
				.param("category", "Electronics"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].category").value("Electronics"));
	}

	@Test
	@DisplayName("GET /category - Fails if no products found (NoSuchElementException)")
	void testGetProductsByCategory_EmptyList() throws Exception {
		when(productService.getProductsByCategory(ArgumentMatchers.anyString(), ArgumentMatchers.any(Pageable.class))).thenReturn(Page.empty());

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/api/products/category").param("category", "Unknown"));
		});

		assertTrue(exception.getCause() instanceof NoSuchElementException);
	}

	@Test
	@DisplayName("GET /{id} - Success")
	void testGetProductDetails_Success() throws Exception {
		when(productService.getProductDetailsById(ArgumentMatchers.any(ProductIdRequestDTO.class))).thenReturn(mockResponseDto);

		mockMvc
			.perform(get("/api/products/100"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(100L));
	}

	@Test
	@DisplayName("GET /{id} - Fails if product not found")
	void testGetProductDetails_NotFound() throws Exception {
		when(productService.getProductDetailsById(ArgumentMatchers.any(ProductIdRequestDTO.class))).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/api/products/999"));
		});

		assertTrue(exception.getCause() instanceof NoSuchElementException);
	}

	@Test
	@DisplayName("POST / - Success (201 Created)")
	void testAddProducts_Success() throws Exception {
		when(productService.addProduct(ArgumentMatchers.any(ProductRequestDTO.class))).thenReturn(mockResponseDto);

		mockMvc
			.perform(post("/api/products/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mockProduct)))
			.andExpect(status().isCreated())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Product added successfully")));
	}

	@Test
	@DisplayName("POST / - Fails due to Empty Name")
	void testAddProducts_EmptyName() throws Exception {
		mockProduct.setName("");

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc
				.perform(post("/api/products/")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(mockProduct)));
		});

		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("Product name cannot be empty"));
	}

	@Test
	@DisplayName("POST / - Fails due to Negative Price")
	void testAddProducts_NegativePrice() throws Exception {
		mockProduct.setPrice(-50.0);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc
				.perform(post("/api/products/")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(mockProduct)));
		});

		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("price must be greater than zero"));
	}

	@Test
	@DisplayName("POST / - Fails due to Negative Stock")
	void testAddProducts_NegativeStock() throws Exception {
		mockProduct.setStock(-5);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc
				.perform(post("/api/products/")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(mockProduct)));
		});

		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("stock cannot be negative"));
	}

	@Test
	@DisplayName("DELETE /{id} - Success (204 No Content)")
	void testDeleteProduct_Success() throws Exception {
		when(productService.deleteProductByIdByAdmin(ArgumentMatchers.any(ProductIdRequestDTO.class))).thenReturn(mockResponseDto);

		mockMvc
			.perform(delete("/api/products/100"))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("DELETE /{id} - Fails if Product Not Found")
	void testDeleteProduct_NotFound() throws Exception {
		when(productService.deleteProductByIdByAdmin(ArgumentMatchers.any(ProductIdRequestDTO.class))).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(delete("/api/products/999"));
		});

		assertTrue(exception.getCause() instanceof NoSuchElementException);
	}

	@Test
	@DisplayName("PUT /{id} - Success (200 OK)")
	void testUpdateProduct_Success() throws Exception {
		when(productService.updateProductDetailsById(ArgumentMatchers.anyLong(), ArgumentMatchers.any(ProductRequestDTO.class))).thenReturn(mockResponseDto);

		mockMvc
			.perform(put("/api/products/100")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mockProduct)))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Product updated Successfully")));
	}

	@Test
	@DisplayName("PUT /{id} - Fails due to Internal Server Error (CustomException)")
	void testUpdateProduct_Fails() throws Exception {
		when(productService.updateProductDetailsById(ArgumentMatchers.anyLong(), ArgumentMatchers.any(ProductRequestDTO.class))).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc
				.perform(put("/api/products/100")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(mockProduct)));
		});

		assertTrue(exception.getCause() instanceof CustomException);
	}

}
