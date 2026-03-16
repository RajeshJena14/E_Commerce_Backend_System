package com.incture.E_Commerce_Backend_System.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.incture.E_Commerce_Backend_System.dto.CartItemRequestDto;
import com.incture.E_Commerce_Backend_System.dto.CartResponseDto;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Manages shopping cart operations (Authenticated Customer only):
 * 		Facilitates adding, updating, viewing, and removing items prior to checkout.
 */
@RestController
@RequestMapping(path = "/api/cart")
@Tag(name = "Cart APIs", description = "Managing your Cart (No ADMIN access)")
public class CartController {
	
	private static final Logger logger = LoggerFactory.getLogger(CartController.class);

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	/**
	 * Adds product to the Customer's cart
	 * Defaults to a quantity of 1 if not specified.
	 */
	@PostMapping(path = "/add/{productId}")
	@Operation(summary = "Add Product to Cart")
	public ResponseEntity<?> addProductToCart(@PathVariable("productId") Long productId,
			@RequestParam(defaultValue = "1", required = false) int quantity) {
		logger.info("Received request to add product ID: {} to cart with quantity: {}", productId, quantity);
		CartItemRequestDto addToCart = new CartItemRequestDto();
		addToCart.setProduct_id(productId);
		addToCart.setQuantity(quantity);

		CartResponseDto result = cartService.addItemToCart(addToCart);

		if (result == null) {
			logger.error("Failed to add product ID: {} to cart. Internal service error.", productId);
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,"Unable to add the product. Please try agin...");
		}
		logger.info("Successfully added product ID: {} to cart. Current cart total: {}", productId, result.getTotal_price());
		return ResponseEntity.status(HttpStatus.CREATED).body("Product added to cart...\n" + result);
	}

	/**
	 * Retrieves the current items of the Customer's cart
	 */
	@GetMapping(path = "/")
	@Operation(summary = "Display your Cart with Cart Items")
	public ResponseEntity<?> showCart() {
		logger.info("Received request to fetch cart details.");
		
		CartResponseDto result = cartService.showCart();
		
		if (result == null) {
			logger.warn("Fetch failed: User's cart is currently empty.");
			throw new CustomException(HttpStatus.NOT_FOUND,"Your cart is currently empty...");
		}
		logger.info("Successfully fetched cart details containing {} unique item(s).", result.getCartItems().size());
		return ResponseEntity.status(HttpStatus.OK).body("Your Cart:\n" + result);
	}

	/**
	 * Displays the total price breakdown of the Customer's cart
	 */
	@GetMapping(path = "/showPrice")
	@Operation(summary = "Display complete breakdown of all the Cart Items and show total price")
	public ResponseEntity<?> showTotalPriceInCartBeforeCheckout() {
		logger.info("Received request to view total cart price before checkout.");
		
		CartResponseDto result = cartService.showTotalPrice();
		
		if (result == null) {
			logger.warn("Total price calculation failed: User's cart is empty.");
			throw new CustomException(HttpStatus.NOT_FOUND,"No items found in your cart...");
		}
		logger.info("Successfully calculated cart total price: {}", result.getTotal_price());
		return ResponseEntity.status(HttpStatus.OK).body("Your Breakdown: \n{\"items\": " + result.getCartItems() + ",\"total_price\": "
				+ result.getTotal_price() + "}\n\nProceed to checkout...");
	}

	/**
	 * Removes a specific item from the Customer's cart entirely
	 */
	@DeleteMapping(path = "/remove/{productId}")
	@Operation(summary = "Remove an Item from Cart")
	public ResponseEntity<?> deleteProductFromCart(@PathVariable("productId") Long productId) {
		logger.info("Received request to remove product ID: {} from cart.", productId);
		CartItemRequestDto deleteFromCart = new CartItemRequestDto();
		deleteFromCart.setProduct_id(productId);
		
		CartResponseDto result = cartService.deleteItemFromCart(deleteFromCart);
		
		if (result == null) {
			logger.error("Failed to remove product ID: {} from cart. Item might not exist.", productId);
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,"Unable to remove item. Please try again...");
		}
		logger.info("Successfully removed product ID: {} from cart. New total price: {}", productId, result.getTotal_price());
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Updates the quantity of a specific item already present in the Customer's cart
	 */
	@PutMapping(path = "/update/{productId}")
	@Operation(summary = "Update Quantity of a Cart Item")
	public ResponseEntity<?> updateProductQuantityToCart(@PathVariable("productId") Long productId,
			@RequestParam(required = true) int quantity) {

		logger.info("Received request to update quantity for product ID: {} to new quantity: {}", productId, quantity);
		CartItemRequestDto updateCartItem = new CartItemRequestDto();
		updateCartItem.setProduct_id(productId);
		updateCartItem.setQuantity(quantity);
		
		CartResponseDto result = cartService.updateQuantityInCart(updateCartItem);

		if (result == null) {
			logger.error("Failed to update quantity for product ID: {}. Item might not be in cart.", productId);
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,"Unable to update quantity. Please try again...");
		}
		logger.info("Successfully updated quantity for product ID: {}. New total price: {}", productId, result.getTotal_price());
		return ResponseEntity.status(HttpStatus.OK).body("Product added to cart...\n" + result);
	}
}
