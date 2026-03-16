package com.incture.E_Commerce_Backend_System.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.incture.E_Commerce_Backend_System.dto.OrderResponseDto;
import com.incture.E_Commerce_Backend_System.dto.OrdersRequestDto;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Handles the Order Lifecycle: 
 * 		Cart Checkout,
 * 		Viewing Order History,
 *  	Allowing ADMINs to update Order Status.
 */
@RestController
@RequestMapping(path = "/api/orders")
@Tag(name = "Order APIs", description = "Checkout Operation, Displaying Orders, and Updating Order Status by ADMIN")
public class OrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	/**
	 * Converts the Authenticated Customer's active cart into an Order after validating the payment amount
	 */
	@PostMapping("/checkout")
	@Operation(summary = "Proceed to Checkout (Mandatory: view Total Price before Checkout)")
	public ResponseEntity<?> checkoutFromCart(@RequestParam(value = "amount", required = true) double amountToPay) {
		logger.info("Received checkout request with payment amount: {}", amountToPay);
		
		OrderResponseDto result = orderService.checkoutFromCart(amountToPay);
		
		if (result == null || result.getPayment_status().equals("FAILED")) {
			logger.error("Checkout failed. Payment validation unsuccessful for amount: {}", amountToPay);
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Checkout Failed. Please verify your payment details...");
		}
		logger.info("Checkout successful! Order ID {} created with total amount: {}", result.getId(), result.getTotal_amount());
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Retrieves a paginated list of all active orders placed by the Customers
	 */
	@GetMapping(path = "/")
	@Operation(summary = "Display all Orders")
	public ResponseEntity<?> getAllOrdersOfUser(@RequestParam(defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "5", required = false) int pageResultsSize,
			@RequestParam(defaultValue = "id", required = false) String sortBy,
			@RequestParam(defaultValue = "true", required = false) boolean ascending) {

		logger.info("Received request to fetch orders. Page: {}, Size: {}, SortBy: {}", page, pageResultsSize, sortBy);
		Sort sort = (ascending) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, pageResultsSize, sort);

		Page<OrderResponseDto> result = orderService.getAllOrders(pageable);

		if (result.isEmpty()) {
			logger.warn("Fetch failed: No orders found for the user.");
			throw new CustomException(HttpStatus.NOT_FOUND, "You haven't placed any orders yet...");
		}
		logger.info("Successfully fetched {} orders.", result.getNumberOfElements());
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Retrieves a paginated list of the Customer's past order history
	 */
	@GetMapping(path = "/history")
	@Operation(summary = "Display Order History of the Logged In User")
	public ResponseEntity<?> getOrdersHistoryOfCurrentUser(@RequestParam(defaultValue = "0", required = false) int page,
			@RequestParam(value = "size", defaultValue = "5", required = false) int pageResultsSize,
			@RequestParam(defaultValue = "orderDate", required = false) String sortBy,
			@RequestParam(defaultValue = "false", required = false) boolean ascending) {

		logger.info("Received request to fetch order history. Page: {}, Size: {}", page, pageResultsSize);
		Sort sort = (ascending) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, pageResultsSize, sort);

		Page<OrderResponseDto> result = orderService.getOrdersHistory(pageable);

		if (result.isEmpty()) {
			logger.warn("Fetch failed: Order history is empty.");
			throw new CustomException(HttpStatus.NOT_FOUND, "You haven't placed any orders yet...");
		}
		logger.info("Successfully fetched {} historical orders.", result.getNumberOfElements());
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Retrieves the specific details of a single order
	 * Restricted to "ADMIN" and "CURRENT-USER" only
	 */
	@GetMapping(path = "/{id}")
	@Operation(summary = "Display a particular Order by Order ID")
	public ResponseEntity<?> getSpecificOrderDetails(@PathVariable("id") long orderId) {
		logger.info("Received request to fetch details for order ID: {}", orderId);
		OrdersRequestDto requestOrder = new OrdersRequestDto();
		requestOrder.setId(orderId);
		
		OrderResponseDto result = orderService.getOrderDetailsById(requestOrder);
		
		if (result == null) {
			logger.error("Fetch failed: Order with ID {} not found or access denied.", orderId);
			throw new CustomException(HttpStatus.NOT_FOUND, "Order with id: " + orderId + " not found...");
		}
		logger.info("Successfully fetched details for order ID: {}", orderId);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Updates the status of an order (e.g., SHIPPED, DELIVERED)
	 * Restricted to "ADMIN" only
	 */
	@PutMapping(path = "/{id}/status")
	@Operation(summary = "Update Order status of a particular Order {ADMIN Access only}")
	public ResponseEntity<?> updateOrderStatusOnlyByAdmin(@PathVariable("id") long orderId,
			@RequestParam(required = true) String status) {
		logger.info("Admin request received to update order ID: {} to new status: '{}'", orderId, status);
		OrdersRequestDto requestOrder = new OrdersRequestDto();
		requestOrder.setId(orderId);
		requestOrder.setOrderStatus(status);
		
		OrderResponseDto result = orderService.updateOrderStatusById(requestOrder);
		
		if (result == null) {
			logger.error("Status update failed for order ID: {}. Invalid transition or order not found.", orderId);
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update Status. Please try again...");
		}
		logger.info("Successfully updated order ID: {} to status: '{}'", orderId, result.getOrder_status());
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

}
