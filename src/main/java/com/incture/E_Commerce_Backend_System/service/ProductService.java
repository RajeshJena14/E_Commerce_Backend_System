package com.incture.E_Commerce_Backend_System.service;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.incture.E_Commerce_Backend_System.dto.ProductIdRequestDTO;
import com.incture.E_Commerce_Backend_System.dto.ProductRequestDTO;
import com.incture.E_Commerce_Backend_System.dto.ProductResponseDTO;
import com.incture.E_Commerce_Backend_System.entity.Product;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.repository.ProductRepository;

/**
 * Handles business logic for the product catalog
 * Manages product retrieval, creation, deletion, and updates, ensuring data integrity
 */
@Service
public class ProductService {

	private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

	private final ProductRepository productRepository;

	private final ModelMapper modelMapper;

	public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
		this.productRepository = productRepository;
		this.modelMapper = modelMapper;
	}

	/**
	 * Retrieves a paginated list of all available products
	 */
	public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
		logger.debug("Fetching all products from the database with pagination.");
		
		// Fetch paginated products from the repository
		Page<Product> productList = productRepository.findAll(pageable);
		
		// Map the resulting Product entities to ProductResponseDTOs
		Page<ProductResponseDTO> result = productList
				.map(product -> modelMapper.map(product, ProductResponseDTO.class));
		logger.debug("Successfully fetched {} products.", productList.getNumberOfElements());
		return result;
	}

	/**
	 * Retrieves a paginated list of products filtered by a specific category
	 */
	public Page<ProductResponseDTO> getProductsByCategory(String category, Pageable pageable) {
		logger.debug("Fetching products for category: '{}'", category);
		
		// Fetch paginated filtered products from the repository
		Page<Product> productList = productRepository.findByCategory(category, pageable);
		
		Page<ProductResponseDTO> result = productList
				.map(product -> modelMapper.map(product, ProductResponseDTO.class));
		logger.debug("Successfully fetched {} products for category: '{}'", productList.getNumberOfElements(),
				category);
		return result;
	}

	/**
	 * Fetches the specific details of a single product by its ID
	 */
	public ProductResponseDTO getProductDetailsById(ProductIdRequestDTO productId) {
		logger.debug("Attempting to fetch product details for ID: {}", productId.getId());
		
		// Retrieve the product or return null if it doesn't exist
		Product product = productRepository.findById(productId.getId()).orElse(null);
		if (product == null) {
			logger.warn("Fetch aborted: No product found in database with ID: {}", productId.getId());
			return null;
		}
		logger.debug("Successfully fetched details for product ID: {}", productId.getId());
		
		return modelMapper.map(product, ProductResponseDTO.class);
	}

	/**
	 * Adds a new product to the catalog
	 * Enforces unique product names
	 */
	public ProductResponseDTO addProduct(ProductRequestDTO productRequest) {
		logger.debug("Attempting to save a new product to the database.");
		
		// Check for existing product to avoid name collisions
		Product existingProduct = productRepository.findByName(productRequest.getName());
		if (existingProduct != null) {
			logger.warn("Add product failed: Product name '{}' already exists.", productRequest.getName());
			throw new CustomException(HttpStatus.CONFLICT,
					"A product with the name '" + productRequest.getName() + "' already exists in the catalog...");
		}
		
		// Map the incoming DTO to a Product entity
		Product addProduct = modelMapper.map(productRequest, Product.class);
		
		// Save the new product to the database
		Product result = null;
		try {
			result = productRepository.save(addProduct);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Database error while saving new product: {}", e.getMessage());
			return null;
		}
		logger.debug("Successfully saved new product with ID: {}", result.getId());
		
		ProductResponseDTO responseProduct = modelMapper.map(result, ProductResponseDTO.class);
		return responseProduct;
	}

	/**
	 * Deletes a product from the database permanently
	 */
	public ProductResponseDTO deleteProductByIdByAdmin(ProductIdRequestDTO productId) {
		logger.debug("Attempting to delete product with ID: {}", productId.getId());
		
		// Verify the product exists before attempting deletion
		Product product = productRepository.findById(productId.getId()).orElse(null);
		if (product == null) {
			logger.warn("Deletion aborted: No product found in database with ID: {}", productId.getId());
			return null;
		}
		
		// Perform the deletion
		productRepository.deleteById(productId.getId());
		
		logger.debug("Successfully deleted product with ID: {}", productId.getId());
		return modelMapper.map(product, ProductResponseDTO.class);
	}

	/**
	 * Updates the details of an existing product
	 * Applies updates to those fields that are explicitly provided
	 */
	public ProductResponseDTO updateProductDetailsById(long id, ProductRequestDTO updatedProduct) {
		logger.debug("Attempting to update details for product ID: {}", id);
		Product oldproduct = productRepository.findById(id).orElse(null);
		if (oldproduct == null) {
			logger.warn("Update aborted: No product found in database with ID: {}", id);
			return null;
		}
		
		// Update fields only if new valid values are provided
		oldproduct.setName(updatedProduct.getName() != null ? updatedProduct.getName() : oldproduct.getName());
		oldproduct.setDescription(updatedProduct.getDescription() != null ? updatedProduct.getDescription()
				: oldproduct.getDescription());
		oldproduct.setPrice(updatedProduct.getPrice() != null ? updatedProduct.getPrice() : oldproduct.getPrice());
		oldproduct.setStock(updatedProduct.getStock() != null ? updatedProduct.getStock() : oldproduct.getStock());
		oldproduct.setCategory(
				updatedProduct.getCategory() != null ? updatedProduct.getCategory() : oldproduct.getCategory());
		oldproduct.setImage_url(
				updatedProduct.getImage_url() != null ? updatedProduct.getImage_url() : oldproduct.getImage_url());
		oldproduct.setRating(updatedProduct.getRating() != null ? updatedProduct.getRating() : oldproduct.getRating());

		// Save the updated product
		Product newproduct = productRepository.save(oldproduct);
		
		logger.debug("Successfully updated details for product ID: {}", id);
		return modelMapper.map(newproduct, ProductResponseDTO.class);
	}

}
