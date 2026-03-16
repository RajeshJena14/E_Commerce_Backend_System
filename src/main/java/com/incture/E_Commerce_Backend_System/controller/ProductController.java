package com.incture.E_Commerce_Backend_System.controller;

import java.util.NoSuchElementException;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.incture.E_Commerce_Backend_System.dto.ProductIdRequestDTO;
import com.incture.E_Commerce_Backend_System.dto.ProductRequestDTO;
import com.incture.E_Commerce_Backend_System.dto.ProductResponseDTO;
import com.incture.E_Commerce_Backend_System.entity.Product;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Product catalog operations:
 * 		Public read access to products
 * 		Restricts modifications (Add, Update, Delete) to ADMINs
 */
@RestController
@RequestMapping(path = "/api/products/")
@Tag(name = "Product APIs", description = "Fetch Products and CRUD operations by ADMIN")
public class ProductController {

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

	private final ProductService productService;

	private final ModelMapper modelMapper;

	public ProductController(ProductService productService, ModelMapper modelMapper) {
		this.productService = productService;
		this.modelMapper = modelMapper;
	}

	/**
	 * Retrieves a paginated list of all products in the catalog
	 * TIPS TO USE: 
	 * 		Values of 'page', 'pageSize', 'sortBy', and 'ascending' can be provided as Parameters
	 * 		E.g: if you want to view page-2, pass page = 2 in URL or pass key as 'page' and value as '2' in Request-Params
	 * 		E.g: if you want to sort products on basis of 'price',
	 * 				pass sortBy = price in URL or pass key as 'sortBy' and value as 'price' in Request-Params
	 * 				( pass ascending = true -> Price: Low to High & ascending = false -> Price: High to Low )
	 */
	@GetMapping(path = "/")
	@Operation(summary = "Fetch all Products from catalog")
	public ResponseEntity<?> getAllProducts(@RequestParam(defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "5", required = false) int pageResultsSize,
			@RequestParam(defaultValue = "id", required = false) String sortBy,
			@RequestParam(defaultValue = "true", required = false) boolean ascending) {

		logger.info("Received request to fetch all products. Page: {}, Size: {}, SortBy: {}", page, pageResultsSize,
				sortBy);
		Sort sort = (ascending) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, pageResultsSize, sort);

		Page<ProductResponseDTO> result = productService.getAllProducts(pageable);

		if (result.isEmpty()) {
			logger.warn("Fetch failed: No products are currently available in the catalog.");
			throw new NoSuchElementException("No products are currently available in the catalog...");
		}
		logger.info("Successfully fetched {} products (Page {}).", result.getNumberOfElements(), page);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Retrieves a paginated list of products filtered by category
	 */
	@GetMapping(path = "/category")
	@Operation(summary = "Fetch Products from catalog by category")
	public ResponseEntity<?> getProductsByCategory(@RequestParam("category") String category,
			@RequestParam(defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "5", required = false) int pageResultsSize,
			@RequestParam(defaultValue = "id", required = false) String sortBy,
			@RequestParam(defaultValue = "true", required = false) boolean ascending) {

		logger.info("Received request to fetch products for category: '{}'. Page: {}, Size: {}", category, page,
				pageResultsSize);
		Sort sort = (ascending) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, pageResultsSize, sort);

		Page<ProductResponseDTO> result = productService.getProductsByCategory(category, pageable);
		
		if (result.isEmpty()) {
			logger.warn("Fetch failed: No products found matching the category: '{}'", category);
			throw new NoSuchElementException("No products found mathcing the category: " + category + "...");
		}
		logger.info("Successfully fetched {} products for category: '{}'", result.getNumberOfElements(), category);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Fetches the specific details of a single product
	 */
	@GetMapping(path = "/{id}")
	@Operation(summary = "Fetch a Product by Product ID from catalog")
	public ResponseEntity<?> getProductDetails(@PathVariable("id") long id) {
		logger.info("Received request to fetch details for product ID: {}", id);
		ProductIdRequestDTO productId = new ProductIdRequestDTO();
		productId.setId(id);
		
		ProductResponseDTO result = productService.getProductDetailsById(productId);
		
		if (result == null) {
			logger.error("Fetch failed: No product found for ID: {}", id);
			throw new NoSuchElementException("No product found for the provided ID: " + id + "...");
		}
		logger.info("Successfully fetched details for product ID: {}", id);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Adds a new product to the catalog
	 * Restricted to "ADMIN" only
	 */
	@PostMapping(path = "/")
	@Operation(summary = "Add a new Product {ADMIN Access only}")
	public ResponseEntity<?> addProducts(@RequestBody Product product) {
		logger.info("Admin request received to add a new product.");
		ProductRequestDTO productRequest = null;
		try {
			productRequest = modelMapper.map(product, ProductRequestDTO.class);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			logger.error("Add product failed: Request Body is missing or invalid.");
			throw new NullPointerException("Request Body is missing or invalid...");
		}

		// Input validation (Mandatory fields)
		if (productRequest.getName() == null || productRequest.getName().trim().isEmpty()) {
			logger.error("Add product failed: Product Name cannot be empty.");
			throw new IllegalArgumentException("Product name cannot be empty...");
		}
		if (productRequest.getDescription() == null || productRequest.getDescription().trim().isEmpty()) {
			logger.error("Add product failed: Product Description cannot be empty.");
			throw new IllegalArgumentException("Product description cannot be empty...");
		}
		if (productRequest.getCategory() == null || productRequest.getCategory().trim().isEmpty()) {
			logger.error("Add product failed: Product Category cannot be empty.");
			throw new IllegalArgumentException("Product category cannot be empty...");
		}
		if (productRequest.getPrice() <= 0.0) {
			logger.error("Add product failed: Product Price must be greater than zero.");
			throw new IllegalArgumentException("Product price must be greater than zero...");
		}
		if (productRequest.getStock() < 0) {
			logger.error("Add product failed: Product Stock cannot be negative.");
			throw new IllegalArgumentException("Product stock cannot be negative...");
		}

		ProductResponseDTO result = productService.addProduct(productRequest);
		
		if (result == null) {
			logger.error("Add product failed: Internal server error or invalid details provided.");
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Invalid registration details. Please ensure all required fields are provided...");
		}
		logger.info("Product added successfully with new ID: {}", result.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body("Product added successfully...\n" + result);
	}

	/**
	 * Removes a product from the catalog entirely
	 * Restricted to "ADMIN" only
	 */
	@DeleteMapping(path = "/{id}")
	@Operation(summary = "Delete an existing Product {ADMIN Access only}")
	public ResponseEntity<?> deleteProductOnlyByAdmin(@PathVariable("id") long id) {
		logger.info("Admin request received to delete product with ID: {}", id);
		ProductIdRequestDTO productId = new ProductIdRequestDTO();
		productId.setId(id);
		
		ProductResponseDTO result = productService.deleteProductByIdByAdmin(productId);
		
		if (result == null) {
			logger.error("Deletion failed: No product found for ID: {}", id);
			throw new NoSuchElementException("No product found for the provided ID: " + id + "...");
		}
		logger.info("Product with ID {} deleted successfully.", id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Updates the details of an existing product
	 * Restricted to "ADMIN" only
	 */
	@PutMapping(path = "/{id}")
	@Operation(summary = "Update details of an existing Product {ADMIN Access only}")
	public ResponseEntity<?> updateProductDetailsOnlyByAdmin(@PathVariable("id") long id,
			@RequestBody Product product) {
		logger.info("Admin request received to update product with ID: {}", id);
		ProductRequestDTO updatedProduct = null;
		try {
			updatedProduct = modelMapper.map(product, ProductRequestDTO.class);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			logger.error("Update failed for product ID {}: Request Body is missing or invalid.", id);
			throw new NullPointerException("Request Body is missing or invalid...");
		}
		
		ProductResponseDTO result = productService.updateProductDetailsById(id, updatedProduct);
		
		if (result == null) {
			logger.error("Update failed for product ID {}: Internal server error during update.", id);
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Product update failed. Try Again...");
		}
		logger.info("Product with ID {} updated successfully.", id);
		return ResponseEntity.status(HttpStatus.OK).body("Product updated Successfully...\n" + result);
	}
}
