package com.incture.E_Commerce_Backend_System.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incture.E_Commerce_Backend_System.dto.UserIdRequestDto;
import com.incture.E_Commerce_Backend_System.dto.UserLoginDto;
import com.incture.E_Commerce_Backend_System.dto.UserRegisterDto;
import com.incture.E_Commerce_Backend_System.dto.UserResponseDto;
import com.incture.E_Commerce_Backend_System.entity.User;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.repository.UserRepository;
import com.incture.E_Commerce_Backend_System.utils.JWTUtils;

/**
 * Handles business logic for user management
 * Facilitates registration, secure authentication, profile retrieval, and administrative actions
 */
@Service
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final AuthenticationManager authManager;

	private final JWTUtils jwtUtils;

	private final UserRepository userRepository;

	private final ModelMapper modelMapper;

	public UserService(AuthenticationManager authManager, JWTUtils jwtUtils, UserRepository userRepository,
			ModelMapper modelMapper) {
		this.authManager = authManager;
		this.jwtUtils = jwtUtils;
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
	}

	/**
	 * Registers a new user in the system.
	 * Enforces strict uniqueness checks for both username and email to prevent duplicates
	 */
	public UserResponseDto addUser(UserRegisterDto user) {
		logger.debug("Attempting to save new user '{}' to the database.", user.getName());

		// Guard against duplicate usernames
		User existingName = userRepository.findByName(user.getName());
		if (existingName != null) {
			logger.warn("Registration failed: Username '{}' is already taken.", user.getName());
			throw new CustomException(HttpStatus.CONFLICT, "Username is already taken. Please choose another...");
		}
		
		// Guard against duplicate emails
		boolean existingEmail = userRepository.existsByEmail(user.getEmail());
		if (existingEmail) {
			logger.warn("Registration failed: Email '{}' is already registered.", user.getEmail());
			throw new CustomException(HttpStatus.CONFLICT,
					"An account with this email already exists. Please log in...");
		}

		// Save to the database
		User registerUser = modelMapper.map(user, User.class);
		User result = null;
		try {
			result = userRepository.save(registerUser);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Database error while saving user '{}': {}", user.getName(), e.getMessage());
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
					"An unexpected error occurred during registration. Please try again...");
		}
		logger.debug("Successfully saved user '{}' with new ID: {}", result.getName(), result.getId());
		
		UserResponseDto responseUser = modelMapper.map(result, UserResponseDto.class);
		return responseUser;
	}

	/**
	 * Authenticates user credentials against the database
	 * Generates and returns a JWT upon successful authentication
	 */
	public String verify(UserLoginDto user) {
		logger.debug("Attempting to authenticate credentials for user: '{}'", user.getName());
		
		try {
			// Delegate authentication to AuthenticationManager --> AuthenticationProvider
			Authentication authentication = authManager
					.authenticate(new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword()));
			
			// Generate token if credentials are valid
			if (authentication.isAuthenticated()) {
				logger.debug("Authentication successful for user: '{}'. Generating JWT...", user.getName());
				return jwtUtils.generateToken(user.getName());
			}
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			logger.warn("Authentication failed for user: '{}'. Reason: {}", user.getName(), e.getMessage());
		}
		return null;
	}

	/**
	 * Deletes a user account permanently
	 * Restricted to "ADMIN" only
	 */
	public UserResponseDto deleteUserByIdByAdmin(UserIdRequestDto userId) {
		logger.debug("Attempting to delete user with ID: {}", userId.getId());
		
		// Fetch the user to ensure their existence
		User user = userRepository.findById(userId.getId()).orElse(null);
		if (user == null) {
			logger.warn("Deletion aborted: No user found in database with ID: {}", userId.getId());
			return null;
		}
		
		// Executing delete from the database
		userRepository.deleteById(userId.getId());
		
		logger.debug("Successfully deleted user with ID: {}", userId.getId());
		return modelMapper.map(user, UserResponseDto.class);
	}

	/**
	 * Retrieves a user's profile details
	 * Enforces strict authorization checks to prevent horizontal privilege escalation
	 */
	public UserResponseDto getUserDetailsById(UserIdRequestDto userId) {
		logger.debug("Attempting to fetch details for user ID: {}", userId.getId());

		// Retrieve the currently authenticated user
		UserResponseDto loggedInUser = new UserResponseDto();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication != null && authentication.getPrincipal() instanceof UserResponseDto) {
			loggedInUser = (UserResponseDto) authentication.getPrincipal();
			
			// Block users from viewing other users' profiles (unless they are ADMIN)
			if (loggedInUser.getId() != userId.getId()) {
				if (!loggedInUser.getRole().equals("ADMIN")) {
					logger.warn(
							"Security block: User ID {} attempted to fetch data for User ID {} without ADMIN privileges.",
							loggedInUser.getId(), userId.getId());
					throw new AccessDeniedException("Access Denied: Admin Rights Required...");
				}
			}
		}

		// Fetch the target user from the database
		User user = userRepository.findById(userId.getId()).orElse(null);
		if (user == null) {
			logger.warn("Fetch aborted: No user found in database with ID: {}", userId.getId());
			throw new NoSuchElementException("User with id: " + userId.getId() + " not found in the database...");
		}

		UserResponseDto responseUser = modelMapper.map(user, UserResponseDto.class);
		
		// Hiding sensitive payload data (like active carts) from non-owners
		if (loggedInUser.getId() != responseUser.getId()) {
			logger.debug("Sanitizing response: Masking cart data from non-owner view.");
			responseUser.setCart(null);
		}
		
		logger.debug("Successfully fetched details for user ID: {}", userId.getId());
		return responseUser;
	}

	/**
	 * Partially updates a user's profile details
	 * Secured at the method level: Only an ADMIN or the specific account owner can execute this
	 */
	@PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
	@Transactional
	public UserResponseDto updateUserDetailsById(Long userId, UserRegisterDto updatedUser) {
		logger.debug("Attempting to update details for user ID: {}", userId);
		
		// Fetch the existing user record
		User olduser = userRepository.findById(userId).orElse(null);
		if (olduser == null) {
			logger.warn("Update aborted: No user found in database with ID: {}", userId);
			throw new NoSuchElementException("User with id: " + userId + " not found in the database...");
		}
		
		// Apply updates to those fields that are explicitly provided
		olduser.setName(updatedUser.getName() != null ? updatedUser.getName() : olduser.getName());
		olduser.setEmail(updatedUser.getEmail() != null ? updatedUser.getEmail() : olduser.getEmail());
		olduser.setPassword(updatedUser.getPassword() != null ? updatedUser.getPassword() : olduser.getPassword());
		olduser.setRole(updatedUser.getRole() != null ? updatedUser.getRole() : olduser.getRole());
		
		// 3. Save the result
		User newUser = userRepository.save(olduser);
		logger.debug("Successfully updated details for user ID: {}", userId);
		return modelMapper.map(newUser, UserResponseDto.class);
	}

	/**
	 * Retrieves all registered users in the system
	 */
	public List<UserResponseDto> getAllUsers() {
		logger.debug("Attempting to fetch all users from the database.");
		
		List<User> userList = userRepository.findAllWithCartAndOrders();
		/* changed from findAll() to a custom finder to avoid LazyInitializationException	*/

		List<UserResponseDto> result = userList.stream().map(user -> {
			UserResponseDto responseUser = modelMapper.map(user, UserResponseDto.class);
			
			// Keep the payload lightweight by stripping massive nested cart arrays
			responseUser.setCart(null);
			return responseUser;
		}).collect(Collectors.toList());
		
		logger.debug("Successfully fetched and mapped {} users.", result.size());
		return result;
	}
}
