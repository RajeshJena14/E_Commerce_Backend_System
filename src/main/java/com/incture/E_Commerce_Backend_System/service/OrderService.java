package com.incture.E_Commerce_Backend_System.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incture.E_Commerce_Backend_System.dto.OrderItemsResponseDto;
import com.incture.E_Commerce_Backend_System.dto.OrderResponseDto;
import com.incture.E_Commerce_Backend_System.dto.OrdersRequestDto;
import com.incture.E_Commerce_Backend_System.dto.UserResponseDto;
import com.incture.E_Commerce_Backend_System.entity.Cart;
import com.incture.E_Commerce_Backend_System.entity.EmailDetails;
import com.incture.E_Commerce_Backend_System.entity.OrderItems;
import com.incture.E_Commerce_Backend_System.entity.Orders;
import com.incture.E_Commerce_Backend_System.entity.Product;
import com.incture.E_Commerce_Backend_System.entity.User;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.repository.CartRepository;
import com.incture.E_Commerce_Backend_System.repository.OrdersRepository;
import com.incture.E_Commerce_Backend_System.repository.ProductRepository;
import com.incture.E_Commerce_Backend_System.repository.UserRepository;

/**
 * Handles all business logic pertaining to the order lifecycle
 * Manages cart-to-order conversions, payment validation, stock deductions, and order state transitions
 */
@Service
public class OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

	private final OrdersRepository ordersRepository;

	private final ModelMapper modelMapper;

	private final UserRepository userRepository;

	private final ProductRepository productRepository;

	private final CartRepository cartRepository;

	private final EmailService emailService;

	public OrderService(OrdersRepository ordersRepository, ModelMapper modelMapper, UserRepository userRepository,
			ProductRepository productRepository, CartRepository cartRepository, EmailService emailService) {
		this.ordersRepository = ordersRepository;
		this.modelMapper = modelMapper;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.cartRepository = cartRepository;
		this.emailService = emailService;
	}

	/**
	 * Safely retrieves the authenticated user details from the security context
	 */
	private UserResponseDto getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() instanceof UserResponseDto) {
			UserResponseDto result = (UserResponseDto) authentication.getPrincipal();
			return result;
		}
		logger.warn("Failed to retrieve User from SecurityContext. Principal is null or invalid.");
		return null;
	}

	/**
	 * Core checkout flow:
	 * 		Converts an active cart into an Order,
	 * 		Deducts inventory stock,
	 * 		Verifies payment amounts,
	 * 		Dispatches confirmation emails
	 */
	@Transactional
	public OrderResponseDto checkoutFromCart(double amountToPay) {

		// Validate the user session and database record
		UserResponseDto loggedInUser = getUser();
		if (loggedInUser == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized access: User session is invalid...");
		}
		final Long userId = loggedInUser.getId();
		logger.debug("Initiating checkout process for user ID: {}", userId);

		User user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			logger.warn("Checkout aborted: User with ID {} not found in database.", userId);
			throw new UsernameNotFoundException("User with id: " + userId + " not found in the database...");
		}
		
		// Retrieving cart and guard against empty checkouts
		Cart cart = cartRepository.findByUserId(userId);
		if (cart == null || cart.getCartItems().isEmpty()) {
			logger.warn("Checkout aborted: Cart is empty for user ID: {}", userId);
			throw new CustomException(HttpStatus.NOT_FOUND, "Your cart is currently empty...");
		}

		// Initializing the base Order entity
		Orders order = new Orders();
		order.setUser(user);
		order.setTotal_amount(cart.getTotal_price());
		order.setOrderDate(LocalDateTime.now());

		// Process items and permanently deduct warehouse stock
		List<OrderItems> addToOrderItems = cart.getCartItems().stream().map(item -> {
			// Check stock right before deduction to prevent overselling
			if (item.getProduct().getStock() < item.getQuantity()) {
				logger.error("Checkout failed: Insufficient stock for product '{}'. Requested: {}, Available: {}",
						item.getProduct().getName(), item.getQuantity(), item.getProduct().getStock());
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Checkout failed: '" + item.getProduct().getName() + "' is out of stock...");
			}
			
			// Deduct inventory immediately
			item.getProduct().setStock(item.getProduct().getStock() - item.getQuantity());
			
			OrderItems orderedItem = new OrderItems(order, item.getProduct(), item.getQuantity(),
					item.getQuantity() * item.getProduct().getPrice());
			order.getOrderItems().add(orderedItem);
			return orderedItem;
		}).collect(Collectors.toList());

		// Payment validation guard (Rollback stock if payment is insufficient)
		if (amountToPay < cart.getTotal_price()) {
			logger.warn("Payment validation failed for user ID: {}. Amount provided: {}, Required: {}", userId,
					amountToPay, cart.getTotal_price());
			order.setPayment_status("FAILED");
			order.setOrder_status("FAILED");
			
			logger.debug("Restoring product stock due to failed payment...");
			order.getOrderItems()
					.forEach(item -> item.getProduct().setStock(item.getProduct().getStock() + item.getQuantity()));
			
			Orders savedOrder = ordersRepository.save(order);
			OrderResponseDto responseOrderPlaced = convertOrderToResponseDto(savedOrder);
			return responseOrderPlaced;
		}
		
		// Payment successful -> Finalize the order
		order.setPayment_status("SUCCESSFUL");
		order.setOrder_status("PLACED");
		Orders savedOrder = ordersRepository.save(order);
		logger.info("Order successfully placed for user ID: {}. Order ID: {}", userId, savedOrder.getId());

		// After successful order, cart is cleared
		logger.debug("Clearing cart for user ID: {}", userId);
		user.setCart(null);
		cartRepository.delete(cart);

		// Send Email after Successful Order
		EmailDetails emailDetails = new EmailDetails();
		emailDetails.setRecipient(savedOrder.getUser().getEmail());
		emailDetails.setSubject("ORDER CONFIRMATION #" + savedOrder.getId());
		emailDetails.setMsgBody("Hi " + savedOrder.getUser().getName()
				+ ",\n\nThank You for your order! We've received it and are now getting it ready for you.\n\nOrder Number: "
				+ savedOrder.getId() + "\nOrder Date: " + savedOrder.getOrderDate() + "\nTotal Amount: "
				+ savedOrder.getTotal_amount() + "\n\nBest Regards,\nIncture");
		emailService.sendSimpleMail(emailDetails);

		OrderResponseDto responseOrderPlaced = convertOrderToResponseDto(savedOrder);
		return responseOrderPlaced;
	}

	/**
	 * Retrieves a paginated list of orders
	 * ADMINs retrieve all system orders; Customers retrieve only their own
	 */
	public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
		UserResponseDto loggedInUser = getUser();
		if (loggedInUser == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized access: User session is invalid...");
		}
		
		if (loggedInUser.getRole().equals("ADMIN")) {
			logger.debug("Admin fetch: Retrieving all orders in the system.");
			Page<Orders> orderList = ordersRepository.findAll(pageable);
			Page<OrderResponseDto> result = orderList.map(order -> convertOrderToResponseDto(order));
			return result;
		} else {
			logger.debug("Customer fetch: Retrieving all orders for user ID: {}", loggedInUser.getId());
			Page<Orders> pageOrderList = ordersRepository.findByUserId(loggedInUser.getId(), pageable);
			Page<OrderResponseDto> result = pageOrderList.map(pageOrder -> convertOrderToResponseDto(pageOrder));
			return result;
		}
	}

	/**
	 * Retrieves a paginated list of the Customer's past order history
	 */
	public Page<OrderResponseDto> getOrdersHistory(Pageable pageable) {
		UserResponseDto loggedInUser = getUser();
		if (loggedInUser == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized access: User session is invalid...");
		}
		
		logger.debug("Fetching order history for user ID: {}", loggedInUser.getId());
		Page<Orders> pageOrderList = ordersRepository.findByUserId(loggedInUser.getId(), pageable);
		Page<OrderResponseDto> result = pageOrderList.map(pageOrder -> convertOrderToResponseDto(pageOrder));
		return result;
	}

	/**
	 * Retrieves the specific details of a single order
	 * Enforces strict ownership and ADMIN privileges
	 */
	public OrderResponseDto getOrderDetailsById(OrdersRequestDto requestOrder) {
		UserResponseDto loggedInUser = getUser();
		if (loggedInUser == null) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized access: User session is invalid...");
		}
		
		logger.debug("Attempting to fetch details for order ID: {}", requestOrder.getId());
		
		Orders resultOrder = ordersRepository.findById(requestOrder.getId()).orElse(null);
		if (resultOrder == null) {
			logger.warn("Fetch aborted: No order found in database with ID: {}", requestOrder.getId());
			return null;
		}
		
		// Access control verification
		// Guard against horizontal privilege escalation (e.g. CUSTOMER A viewing CUSTOMER B's order)
		boolean isAdmin = loggedInUser.getRole().equals("ADMIN");
		boolean isCurrentUser = resultOrder.getUser().getId() == loggedInUser.getId();
		if (!isAdmin && !isCurrentUser) {
			logger.warn("Security block: User ID {} attempted to access Order ID {} without permission.",
					loggedInUser.getId(), requestOrder.getId());
			throw new AccessDeniedException("Permission denied...");
		}
		
		logger.debug("Successfully fetched details for order ID: {}", requestOrder.getId());
		OrderResponseDto responseOrderPlaced = convertOrderToResponseDto(resultOrder);
		return responseOrderPlaced;
	}

	/**
	 * Updates the status of an existing order
	 * Follows strict rules (e.g., cannot cancel an already shipped order)
	 * Also Manages inventory rollback for cancellations.
	 */
	@Transactional
	public OrderResponseDto updateOrderStatusById(OrdersRequestDto requestOrder) {
		logger.debug("Attempting to update status of order ID: {} to '{}'", requestOrder.getId(),
				requestOrder.getOrderStatus());
		Orders oldOrder = ordersRepository.findById(requestOrder.getId()).orElse(null);
		if (oldOrder == null) {
			logger.warn("Status update aborted: Order with ID {} not found.", requestOrder.getId());
			throw new NoSuchElementException("Order with id: " + requestOrder.getId() + " not found...");
		}

		// Allowed flows: PLACED -> CANCELLED  ||  PLACED -> SHIPPED -> DELIVERED

		if (requestOrder.getOrderStatus().equalsIgnoreCase("CANCELLED")) {
			boolean currentStatusRequired = oldOrder.getOrder_status().equalsIgnoreCase("PLACED");
			if (!currentStatusRequired) {
				logger.warn("Invalid transition: Cannot cancel order ID {}. Current status is '{}'", oldOrder.getId(),
						oldOrder.getOrder_status());
				return null;
			}

			// When an order is cancelled, products must go back into the warehouse stock
			logger.debug("Order ID {} cancelled. Restoring product inventory...", oldOrder.getId());
			oldOrder.getOrderItems().forEach(item -> {
				Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
				if (product != null) {
					product.setStock(product.getStock() + item.getQuantity());
				} else {
					Product addProduct = item.getProduct();
					addProduct.setStock(item.getQuantity());
					productRepository.save(addProduct);
				}
			});
		} else if (requestOrder.getOrderStatus().equalsIgnoreCase("SHIPPED")) {
			boolean currentStatusRequired = oldOrder.getOrder_status().equalsIgnoreCase("PLACED");
			if (!currentStatusRequired) {
				logger.warn("Invalid transition: Cannot ship order ID {}. Current status is '{}'", oldOrder.getId(),
						oldOrder.getOrder_status());
				return null;
			}
		} else if (requestOrder.getOrderStatus().equalsIgnoreCase("DELIVERED")) {
			boolean currentStatusRequired = oldOrder.getOrder_status().equalsIgnoreCase("SHIPPED");
			if (!currentStatusRequired) {
				logger.warn("Invalid transition: Cannot mark order ID {} as delivered. Current status is '{}'",
						oldOrder.getId(), oldOrder.getOrder_status());
				return null;
			}
		} else {
			logger.error("Status update failed: '{}' is not a recognized valid status.", requestOrder.getOrderStatus());
			throw new IllegalArgumentException("Status update not allowed...");
		}

		oldOrder.setOrder_status(requestOrder.getOrderStatus());
		Orders newOrder = ordersRepository.save(oldOrder);

		logger.info("Successfully updated order ID: {} to status: '{}'", newOrder.getId(), newOrder.getOrder_status());
		OrderResponseDto responseOrderPlaced = convertOrderToResponseDto(newOrder);
		return responseOrderPlaced;
	}

	private OrderResponseDto convertOrderToResponseDto(Orders order) {
		if (order == null) {
			return null;
		}
		List<OrderItemsResponseDto> resultList = order.getOrderItems().stream().map(orderProduct -> {
			OrderItemsResponseDto responseOrderItem = modelMapper.map(orderProduct, OrderItemsResponseDto.class);
			responseOrderItem.setProductId(orderProduct.getProduct().getId());
			responseOrderItem.setProductName(orderProduct.getProduct().getName());
			responseOrderItem.setProductCategory(orderProduct.getProduct().getCategory());
			return responseOrderItem;
		}).collect(Collectors.toList());

		OrderResponseDto responseOrder = modelMapper.map(order, OrderResponseDto.class);
		responseOrder.setUserId(order.getUser().getId());
		responseOrder.setOrderItems(resultList);

		return responseOrder;
	}
}