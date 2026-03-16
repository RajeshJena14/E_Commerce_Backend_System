package com.incture.E_Commerce_Backend_System.exception;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler: intercepts exceptions thrown across all controllers and service layers
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Handles application-specific business logic exceptions
	 * Returns the dynamic HTTP status defined within the CustomException itself
	 */
	@ExceptionHandler(value = { CustomException.class })
	public ResponseEntity<String> handleCustomException(CustomException e) {
		return new ResponseEntity<String>(e.getMessage(), e.getStatus());
	}
	
	/**
	 * Handles input validation errors (e.g., missing required fields, negative prices)
	 * Returns 400 BAD REQUEST
	 */
	@ExceptionHandler(value = { IllegalArgumentException.class })
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
		return new ResponseEntity<String>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * Intercepts null pointer errors, typically caused by missing or malformed Request Bodies
	 * Returns 500 INTERNAL SERVER ERROR
	 */
	@ExceptionHandler(value = { NullPointerException.class })
	public ResponseEntity<String> handleNullPointerException(NullPointerException e) {
		return new ResponseEntity<String>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * Handles cases where a requested database record (User, Product, Order) does not exist
	 * Returns 404 NOT FOUND
	 */
	@ExceptionHandler(value = { NoSuchElementException.class })
	public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException e) {
		return new ResponseEntity<String>(e.getLocalizedMessage(), HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Handles unauthenticated access attempts (e.g., missing or invalid JWT token)
	 * Returns 401 UNAUTHORIZED
	 */
	@ExceptionHandler(value = { AuthenticationCredentialsNotFoundException.class })
	public ResponseEntity<String> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException e) {
		return new ResponseEntity<String>(e.getLocalizedMessage(), HttpStatus.UNAUTHORIZED);
	}
	
	/**
	 * Handles scenarios specifically where a requested Username is not found in the database
	 * Returns 404 NOT FOUND
	 */
	@ExceptionHandler(value = { UsernameNotFoundException.class })
	public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException e) {
		return new ResponseEntity<String>(e.getLocalizedMessage(), HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Handles authorization failures (e.g., a Customer trying to access an Admin-only endpoint)
	 * Returns 403 FORBIDDEN
	 */
	@ExceptionHandler(value = { AccessDeniedException.class })
	public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
		return new ResponseEntity<String>(e.getLocalizedMessage(), HttpStatus.FORBIDDEN);
	}
	
	/**
	 * Catch-all fallback handler for any other unexpected application crashes
	 * Returns 500 INTERNAL SERVER ERROR
	 */
	@ExceptionHandler(value = { RuntimeException.class })
	public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
		return new ResponseEntity<String>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
