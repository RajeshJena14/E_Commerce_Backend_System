package com.incture.E_Commerce_Backend_System.service;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.incture.E_Commerce_Backend_System.dto.UserResponseDto;
import com.incture.E_Commerce_Backend_System.entity.User;
import com.incture.E_Commerce_Backend_System.repository.UserRepository;

/**
 * Retrieves user details from the database based on the provided username
 * Essential for Authentication and JWT Validation process
 */
@Service
public class DemoUserService implements UserDetailsService {
	
	private static final Logger logger = LoggerFactory.getLogger(DemoUserService.class);

	private final UserRepository userRepository;

	private final ModelMapper modelMapper;

	public DemoUserService(UserRepository userRepository, ModelMapper modelMapper) {
		this.userRepository = userRepository;
		this.modelMapper = modelMapper;
	}

	/**
	* Loads a user's details from the database during the login/JWT validation process
	*/
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		logger.debug("Attempting to load user details for username: '{}'", username);
		
		// Fetch the raw user entity from the database
		User user = userRepository.findByName(username);
		
		// Ensuring user actually exists before proceeding
		if (user == null) {
			logger.warn("Authentication failed: User with username '{}' not found in the database.", username);
			throw new UsernameNotFoundException("User with username: " + username + " not found...");
		}
		logger.debug("Successfully loaded user details for username: '{}'", username);
		
		// Map the database entity to a DTO
		UserResponseDto responseUser = modelMapper.map(user, UserResponseDto.class);
		return responseUser;
	}

}
