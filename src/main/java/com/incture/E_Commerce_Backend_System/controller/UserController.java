package com.incture.E_Commerce_Backend_System.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.incture.E_Commerce_Backend_System.dto.UserIdRequestDto;
import com.incture.E_Commerce_Backend_System.dto.UserLoginDto;
import com.incture.E_Commerce_Backend_System.dto.UserRegisterDto;
import com.incture.E_Commerce_Backend_System.dto.UserResponseDto;
import com.incture.E_Commerce_Backend_System.entity.User;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * User-related operations => Registration, Login, Profile updates, ADMIN-level user management, etc.
 */
@RestController
@RequestMapping(path = "/api/users")
@Tag(name = "User APIs", description = "Registration, Login, Profile View, Profile Updates and ADMIN-level User Management")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	private final UserService userService;

	// Using BcryptPasswordEncoder with strength 12
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	private final ModelMapper modelMapper;

	public UserController(UserService userService, ModelMapper modelMapper) {
		this.userService = userService;
		this.bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
		this.modelMapper = modelMapper;
	}

	/**
	 * Registers a new user
	 * TIPS TO REMEMBER:
	 * 		Request Body cannot be "null"
	 * 		Name, Email and Password fields cannot be empty
	 * 		Defaults role = CUSTOMER, if Role field is left empty
	 */
	@PostMapping(path = "/register")
	@Operation(summary = "Register a new User (Mandatory Fields: Name, Email, Password)")
	public ResponseEntity<?> registerUser(@RequestBody User user) {
		logger.info("Received request to register a new user.");
		UserRegisterDto userDtoForRegister = null;
		try {
			userDtoForRegister = modelMapper.map(user, UserRegisterDto.class);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			logger.error("Registration failed: Request Body is missing or invalid.");
			throw new NullPointerException("Request Body is missing or invalid...");
		}
		
		// Validating required fields (Name, Email, Password) with Role formatting
		if (userDtoForRegister.getName() == null || userDtoForRegister.getName().trim().isEmpty()) {
			logger.error("Registration failed: Name cannot be empty.");
			throw new IllegalArgumentException("Name cannot be empty...");
		}
		if (userDtoForRegister.getEmail() == null || userDtoForRegister.getEmail().trim().isEmpty()) {
			logger.error("Registration failed: Email cannot be empty.");
			throw new IllegalArgumentException("Email cannot be empty...");
		}
		if (userDtoForRegister.getRole() == null || userDtoForRegister.getRole().trim().isEmpty()) {
			userDtoForRegister.setRole("CUSTOMER");
		} else {
			userDtoForRegister.setRole(userDtoForRegister.getRole().toUpperCase());
		}
		if (userDtoForRegister.getPassword() == null || userDtoForRegister.getPassword().isEmpty()) {
			logger.error("Registration failed: Password cannot be empty.");
			throw new IllegalArgumentException("Password cannot be empty...");
		}
		
		// Encoding Password before sending to Service Layer
		userDtoForRegister.setPassword(bCryptPasswordEncoder.encode(userDtoForRegister.getPassword()));

		UserResponseDto result = userService.addUser(userDtoForRegister);
		
		if (result == null) {
			logger.error("Registration failed: Internal Server Error.");
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error. Please try again...");
		}
		logger.info("User '{}' registered successfully with ID: {}", result.getName(), result.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	/**
	 * Authenticates a user
	 * Returns a JWT token in the Authorization header
	 * TIPS TO REMEMBER:
	 * 		Request Body cannot be "null"
	 * 		Name and Password fields cannot be empty
	 */
	@PostMapping(path = "/login")
	@Operation(summary = "Login existing User (Mandatory Fields: Name, Password) {New User: First Register then Login}")
	public ResponseEntity<?> loginStatus(@RequestBody User user) {
		logger.info("Received login request for user: {}", user.getName());
		UserLoginDto userDtoForLogin = null;
		try {
			userDtoForLogin = modelMapper.map(user, UserLoginDto.class);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			logger.error("Login failed: Request Body is missing or invalid.");
			throw new NullPointerException("Request Body is missing or invalid...");
		}
		
		// Validating Name and Password
		if (userDtoForLogin.getName() == null || userDtoForLogin.getName().trim().isEmpty()) {
			logger.error("Login failed: Request Body is missing or invalid.");
			throw new IllegalArgumentException("Username cannot be empty...");
		}
		if (userDtoForLogin.getPassword() == null || userDtoForLogin.getPassword().isEmpty()) {
			logger.error("Login failed: Request Body is missing or invalid.");
			throw new IllegalArgumentException("Password cannot be empty...");
		}
		
		String result = userService.verify(userDtoForLogin);
		
		if (result == null) {
			logger.warn("Login failed: Invalid credentials provided for user: {}", userDtoForLogin.getName());
			throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid login details...");
		}
		logger.info("User '{}' logged in successfully.", userDtoForLogin.getName());
		return ResponseEntity.status(HttpStatus.OK).header("Authorization", "Bearer " + result)
				.body("Login Successful. Get your JWT Token from Header...");
	}

	/**
	 * Deletes a user by ID
	 * Restricted to "ADMIN" only
	 */
	@DeleteMapping(path = "/{id}")
	@Operation(summary = "Delete an existing User {ADMIN Access only}")
	public ResponseEntity<?> deleteUserOnlyByAdmin(@PathVariable("id") long id) {
		logger.info("Admin request received to delete user with ID: {}", id);
		UserIdRequestDto userId = new UserIdRequestDto();
		userId.setId(id);
		
		UserResponseDto result = userService.deleteUserByIdByAdmin(userId);
		
		if (result == null) {
			logger.error("Deletion failed: User with ID {} not found.", id);
			throw new UsernameNotFoundException("User with id = " + userId.getId() + " not found...");
		}
		logger.info("User with ID {} deleted successfully.", id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	/**
	 * Retrieves the profile details for a specific user
	 */
	@GetMapping(path = "/{id}")
	@Operation(summary = "Fetch User by User ID")
	public ResponseEntity<?> getUserDetails(@PathVariable("id") long id) {
		logger.info("Received request to fetch details for user ID: {}", id);
		UserIdRequestDto userId = new UserIdRequestDto();
		userId.setId(id);
		
		UserResponseDto result = userService.getUserDetailsById(userId);
		
		if (result == null) {
			logger.error("Fetch failed: Unable to retrieve user with ID {}.", id);
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "User fetch failed. Try Again...");
		}
		logger.info("Successfully fetched details for user ID: {}", id);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Retrieves all registered users
	 * Restricted to "ADMIN" only
	 */
	@GetMapping(path = "/")
	@Operation(summary = "Fetch all Users")
	public ResponseEntity<?> getAllUserDetails() {
		logger.info("Received request to fetch all users.");
		
		List<UserResponseDto> result = userService.getAllUsers();
		
		if (result.size() <= 0) {
			logger.warn("Fetch failed: No users found in the database.");
			throw new NoSuchElementException("No users found in the database...");
		}
		logger.info("Successfully fetched {} users.", result.size());
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Updates an existing user's details
	 * Hashes the new password if provided
	 * Restricted to "ADMIN" and "CURRENT-USER" only
	 * CURRENT-USER = User ID passed in URL matches the User ID in JWT-Token
	 */
	@PutMapping(path = "/{id}")
	@Operation(summary = "Update User Details by User ID")
	public ResponseEntity<?> updateUserDetails(@PathVariable("id") long id, @RequestBody User user) {
		logger.info("Received request to update user with ID: {}", id);
		UserRegisterDto updatedUser = null;
		try {
			updatedUser = modelMapper.map(user, UserRegisterDto.class);
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			logger.error("Update failed for user ID {}: Request Body is missing or invalid.", id);
			throw new NullPointerException("Request Body is missing or invalid...");
		}
		
		// Encode new password if the user is updating it
		if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
			updatedUser.setPassword(bCryptPasswordEncoder.encode(updatedUser.getPassword()));
		}
		
		UserResponseDto result = userService.updateUserDetailsById(id, updatedUser);
		
		if (result == null) {
			logger.error("Update failed for user ID {}: Internal server error during update.", id);
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,"User update failed. Try Again...");
		}
		logger.info("User with ID {} updated successfully.", id);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}
