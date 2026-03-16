package com.incture.E_Commerce_Backend_System.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incture.E_Commerce_Backend_System.dto.CartItemRequestDto;
import com.incture.E_Commerce_Backend_System.dto.CartItemResponseDto;
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

/**
 * Handles business logic for shopping cart operations
 * Manages item addition, removal, quantity updates, and total price calculations
 */
@Service
public class CartService {

	private static final Logger logger = LoggerFactory.getLogger(CartService.class);

	private final CartRepository cartRepository;

	private final CartItemRepository cartItemRepository;

	private final UserRepository userRepository;

	private final ProductRepository productRepository;

	private final ModelMapper modelMapper;

	public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
			UserRepository userRepository, ProductRepository productRepository, ModelMapper modelMapper) {
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.modelMapper = modelMapper;
	}

	/**
	 * Retrieves the ID of the currently authenticated user from the Security Context
	 */
	private Long getUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof UserResponseDto) {
			UserResponseDto result = (UserResponseDto) authentication.getPrincipal();
			return result.getId();
		}
		logger.warn("Failed to retrieve User ID from SecurityContext. Principal is null or invalid.");
		return null;
	}

	private boolean InsufficientStock(int productStock, int requiredQuantity) {
		return productStock < requiredQuantity;
	}

	/**
	 * Adds a product to the user's cart or increments its quantity if it already exists
	 * Strict inventory stock checks before adding to cart
	 */
	public CartResponseDto addItemToCart(CartItemRequestDto addToCart) {
		
		// Validating the user session and database record
		logger.debug("Attempting to add product ID: {} to cart. Quantity: {}", addToCart.getProduct_id(),
				addToCart.getQuantity());
		Long userId = getUserId();
		if (userId == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized access: User session is invalid...");
		}
		User user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			logger.warn("Add to cart aborted: User with ID {} not found in database.", userId);
			throw new NoSuchElementException("User with id: " + userId + " not found in the database...");
		}

		// Retrieving existing cart or initialize a fresh one
		Cart cart = cartRepository.findByUserId(userId);
		if (cart == null) {
			logger.debug("No existing cart found for user ID: {}. Creating a new cart.", userId);
			cart = new Cart();
			cart.setUser(user);
			cart.setTotal_price(0.0);
			cart = cartRepository.save(cart);
		}

		// Verifying the product exists and has available inventory
		Product product = productRepository.findById(addToCart.getProduct_id()).orElse(null);
		if (product == null) {
			logger.warn("Add to cart aborted: Product with ID {} not found.", addToCart.getProduct_id());
			return null;
		}
		if (product.getStock() == 0) {
			logger.warn("Add to cart failed: Product ID {} is currently out of stock.", product.getId());
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "This item is currently out of stock...");
		}

		List<CartItem> list = cart.getCartItems();
		CartItem cartitems = cartItemRepository.findByCartAndProduct(cart, product);
		
		// Item doesn't exist in cart yet
		if (cartitems == null) {
			logger.debug("Product ID {} is not yet in the cart. Adding as a new item.", product.getId());
			cartitems = new CartItem();
			cartitems.setProduct(product);
			
			if (InsufficientStock(product.getStock(), addToCart.getQuantity())) {
				logger.warn("Insufficient stock for product ID {}. Requested: {}, Available: {}", product.getId(),
						addToCart.getQuantity(), product.getStock());
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Insufficient stock. Only "
						+ product.getStock() + " units are currently available for this item...");
			}
			
			cartitems.setQuantity(addToCart.getQuantity());
			cartitems.setCart(cart);
			list.add(cartitems);
		}
		// Item already exists, increment quantity
		else {
			logger.debug("Product ID {} already exists in the cart. Updating quantity.", product.getId());
			
			if (InsufficientStock(product.getStock(), cartitems.getQuantity() + addToCart.getQuantity())) {
				logger.warn(
						"Insufficient stock to increase quantity for product ID {}. Requested Total: {}, Available: {}",
						product.getId(), (cartitems.getQuantity() + addToCart.getQuantity()), product.getStock());
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Insufficient stock. Only "
						+ product.getStock() + " units are currently available for this item...");
			}
			
			cartitems.setQuantity(cartitems.getQuantity() + addToCart.getQuantity());
		}

		// Recalculating cart total
		Double totalPrice = list.stream().map(t -> t)
				.collect(Collectors.summingDouble(item -> item.getProduct().getPrice() * item.getQuantity()));
		cart.setTotal_price(totalPrice);

		Cart savedCart = cartRepository.save(cart);

		CartResponseDto responseCart = convertToResponseDto(savedCart);
		logger.debug("Successfully updated cart for user ID {}. New cart total: {}", userId,
				savedCart.getTotal_price());
		return responseCart;
	}

	/**
	 * Retrieves the current contents of the Customer's shopping cart
	 * Returns null if the cart is empty or does not exist
	 */
	public CartResponseDto showCart() {
		Long userId = getUserId();
		if (userId == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized access: User session is invalid...");
		}
		
		logger.debug("Fetching cart details for user ID: {}", userId);
		
		Cart cart = cartRepository.findByUserId(userId);
		if (cart == null || cart.getCartItems().isEmpty()) {
			logger.warn("Cart is empty or not found for user ID: {}", userId);
			return null;
		}
		logger.debug("Successfully retrieved cart for user ID: {}", userId);
		
		CartResponseDto responseCart = convertToResponseDto(cart);
		return responseCart;
	}

	/**
	 * Calculates and retrieves the total price breakdown of the Customer's active cart
	 * Display the final cost before initiating the checkout process
	 */
	public CartResponseDto showTotalPrice() {
		Long userId = getUserId();
		if (userId == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized access: User session is invalid...");
		}
		
		logger.debug("Calculating total cart price for user ID: {}", userId);
		
		Cart cart = cartRepository.findByUserId(userId);
		if (cart == null || cart.getCartItems().isEmpty()) {
			logger.warn("Cannot calculate total price. Cart is empty for user ID: {}", userId);
			return null;
		}
		logger.debug("Total price for user ID {} is {}", userId, cart.getTotal_price());
		
		return convertToResponseDto(cart);
	}

	/**
	 * Removes a specific item from the cart entirely and recalculates the total price
	 */
	@Transactional
	public CartResponseDto deleteItemFromCart(CartItemRequestDto deleteFromCart) {
		Long userId = getUserId();
		if (userId == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized access: User session is invalid...");
		}
		
		logger.debug("Attempting to delete product ID: {} from cart for user ID: {}", deleteFromCart.getProduct_id(),
				userId);
		
		Cart cart = cartRepository.findByUserId(userId);
		// Guard against empty or non-existent carts
		if (cart == null || cart.getCartItems().isEmpty()) {
			logger.warn("Deletion aborted: Cart is currently empty for user ID: {}", userId);
			throw new CustomException(HttpStatus.NOT_FOUND, "Your cart is currently empty...");
		}

		// Locating the specific CartItem matching the requested Product ID
		CartItem deleteCartItem = cart.getCartItems().stream()
				.filter(item -> item.getProduct().getId() == deleteFromCart.getProduct_id()).findFirst().orElse(null);
		if (deleteCartItem == null) {
			logger.warn("Deletion aborted: Product ID {} not found in cart for user ID: {}",
					deleteFromCart.getProduct_id(), userId);
			throw new NoSuchElementException(
					"No product found for the provided ID: " + deleteFromCart.getProduct_id() + "...");
		}
		
		// Removing the item and update the cart's overall total price
		cart.getCartItems().remove(deleteCartItem);
		Double totalPrice = cart.getCartItems().stream().map(t -> t)
				.collect(Collectors.summingDouble(item -> item.getProduct().getPrice() * item.getQuantity()));
		cart.setTotal_price(totalPrice);

		Cart newSavedCart = cartRepository.save(cart);

		logger.debug("Successfully deleted item from cart for user ID: {}. New cart total: {}", userId,
				newSavedCart.getTotal_price());
		CartResponseDto responseCart = convertToResponseDto(newSavedCart);
		return responseCart;
	}

	/**
	 * Updates the exact quantity of a specific item already in the cart
	 * Also enforces stock limits
	 */
	@Transactional
	public CartResponseDto updateQuantityInCart(CartItemRequestDto updateCartItem) {
		Long userId = getUserId();
		if (userId == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized access: User session is invalid...");
		}
		
		logger.debug("Attempting to update quantity for product ID: {} in cart for user ID: {}",
				updateCartItem.getProduct_id(), userId);
		
		Cart cart = cartRepository.findByUserId(userId);
		// Guard against empty or non-existent carts
		if (cart == null || cart.getCartItems().isEmpty()) {
			logger.warn("Update aborted: Cart is empty or not found for user ID: {}", userId);
			return null;
		}

		// Ensuring the product actually exists in the user's cart before attempting an update
		boolean exists = cart.getCartItems().stream()
				.anyMatch(item -> item.getProduct().getId() == updateCartItem.getProduct_id());
		if (!exists) {
			logger.warn("Update aborted: Product ID {} not found in cart for user ID: {}",
					updateCartItem.getProduct_id(), userId);
			throw new NoSuchElementException("Item not found in cart...");
		}

		// Applying the updated quantity (fail and rollback if requested quantity exceeds warehouse stock)
		List<CartItem> updatedCartList = cart.getCartItems().stream().map(item -> {
			if (item.getProduct().getId() == updateCartItem.getProduct_id()) {
				if (item.getProduct().getStock() < updateCartItem.getQuantity()) {
					logger.warn("Insufficient stock to update quantity for product ID {}. Requested: {}, Available: {}",
							item.getProduct().getId(), updateCartItem.getQuantity(), item.getProduct().getStock());
					throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Insufficient stock. Only "
							+ item.getProduct().getStock() + " units are currently available for this item...");
				}
				item.setQuantity(updateCartItem.getQuantity());
			}
			return item;
		}).collect(Collectors.toList());

		// Recalculating total cart value reflecting the new quantity
		Double totalPrice = updatedCartList.stream().map(t -> t)
				.collect(Collectors.summingDouble(item -> item.getProduct().getPrice() * item.getQuantity()));
		cart.setTotal_price(totalPrice);

		Cart updatedCart = cartRepository.save(cart);

		logger.debug("Successfully updated cart quantity for user ID: {}. New cart total: {}", userId, updatedCart.getTotal_price());
		CartResponseDto responseCart = convertToResponseDto(updatedCart);
		return responseCart;
	}

	private CartResponseDto convertToResponseDto(Cart savedCart) {
		if (savedCart == null) {
			return null;
		}
		List<CartItemResponseDto> resultList = savedCart.getCartItems().stream().map(cartProduct -> {
			CartItemResponseDto responseCartItem = modelMapper.map(cartProduct, CartItemResponseDto.class);
			responseCartItem.setProductId(cartProduct.getProduct().getId());
			responseCartItem.setProductName(cartProduct.getProduct().getName());
			responseCartItem.setProductCategory(cartProduct.getProduct().getCategory());
			responseCartItem.setProductPrice(cartProduct.getProduct().getPrice());
			return responseCartItem;
		}).collect(Collectors.toList());

		CartResponseDto responseCart = modelMapper.map(savedCart, CartResponseDto.class);
		responseCart.setUserId(savedCart.getUser().getId());
		responseCart.setCartItems(resultList);

		return responseCart;
	}
}
