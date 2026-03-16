package com.incture.E_Commerce_Backend_System.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.incture.E_Commerce_Backend_System.dto.UserIdRequestDto;
import com.incture.E_Commerce_Backend_System.dto.UserLoginDto;
import com.incture.E_Commerce_Backend_System.dto.UserRegisterDto;
import com.incture.E_Commerce_Backend_System.dto.UserResponseDto;
import com.incture.E_Commerce_Backend_System.entity.Cart;
import com.incture.E_Commerce_Backend_System.entity.User;
import com.incture.E_Commerce_Backend_System.exception.CustomException;
import com.incture.E_Commerce_Backend_System.repository.UserRepository;
import com.incture.E_Commerce_Backend_System.utils.JWTUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthenticationManager authManager;

	@Mock
	private JWTUtils jwtUtils;

	@Spy
	private ModelMapper modelMapper = new ModelMapper();

	@InjectMocks
	private UserService userService;

	private User mockCustomer;

	private User mockAdmin;

	private UserResponseDto mockCustomerLoggedIn;

	private UserResponseDto mockAdminLoggedIn;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing User Service...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested User Service...");
	}

	@BeforeEach
	void setUp() {
		mockCustomer = new User();
		ReflectionTestUtils.setField(mockCustomer, "id", 1L);
		mockCustomer.setName("TestUser");
		mockCustomer.setEmail("test@gmail.com");
		mockCustomer.setPassword("hashedPass123");
		mockCustomer.setRole("CUSTOMER");

		Cart customerCart = new Cart();
		ReflectionTestUtils.setField(customerCart, "id", 10L);
		mockCustomer.setCart(customerCart);

		mockCustomerLoggedIn = new UserResponseDto();
		mockCustomerLoggedIn.setId(1L);
		mockCustomerLoggedIn.setName("TestUser");
		mockCustomerLoggedIn.setRole("CUSTOMER");

		mockAdmin = new User();
		ReflectionTestUtils.setField(mockAdmin, "id", 99L);
		mockAdmin.setName("AdminUser");
		mockAdmin.setRole("ADMIN");

		mockAdminLoggedIn = new UserResponseDto();
		mockAdminLoggedIn.setId(99L);
		mockAdminLoggedIn.setName("AdminUser");
		mockAdminLoggedIn.setRole("ADMIN");
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
		mockCustomer = null;
		mockAdmin = null;
		mockCustomerLoggedIn = null;
		mockAdminLoggedIn = null;
	}

	private void mockSecurityContext(UserResponseDto principal) {
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication authentication = mock(Authentication.class);

		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(principal);

		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	@DisplayName("Test for Registration - Success")
	void testAddUser_Success() {
		UserRegisterDto registerDto = new UserRegisterDto();
		registerDto.setName("NewUser");
		registerDto.setEmail("new@gmail.com");

		when(userRepository.findByName("NewUser")).thenReturn(null);
		when(userRepository.existsByEmail("new@gmail.com")).thenReturn(false);
		when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(mockCustomer);

		UserResponseDto result = userService.addUser(registerDto);

		assertNotNull(result);
		assertEquals("TestUser", result.getName());
		verify(userRepository, times(1)).save(ArgumentMatchers.any(User.class));
	}

	@Test
	@DisplayName("Test for Registration - Fails due to Duplicate Username")
	void testAddUser_DuplicateName() {
		UserRegisterDto registerDto = new UserRegisterDto();
		registerDto.setName("TestUser");

		when(userRepository.findByName("TestUser")).thenReturn(mockCustomer);

		CustomException ex = assertThrows(CustomException.class, () -> userService.addUser(registerDto));

		assertEquals(HttpStatus.CONFLICT, ex.getStatus());
		verify(userRepository, never()).save(ArgumentMatchers.any(User.class));
	}

	@Test
	@DisplayName("Test for Registration - Fails due to Duplicate Email")
	void testAddUser_DuplicateEmail() {
		UserRegisterDto registerDto = new UserRegisterDto();
		registerDto.setName("UniqueName");
		registerDto.setEmail("test@gmail.com");

		when(userRepository.findByName("UniqueName")).thenReturn(null);
		when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);

		CustomException ex = assertThrows(CustomException.class, () -> userService.addUser(registerDto));

		assertEquals(HttpStatus.CONFLICT, ex.getStatus());
		verify(userRepository, never()).save(ArgumentMatchers.any(User.class));
	}

	@Test
	@DisplayName("Test for Registration - Fails due to Database Save Error")
	void testAddUser_DatabaseError() {
		UserRegisterDto registerDto = new UserRegisterDto();
		registerDto.setName("NewUser");
		registerDto.setEmail("new@gmail.com");

		when(userRepository.findByName("NewUser")).thenReturn(null);
		when(userRepository.existsByEmail("new@gmail.com")).thenReturn(false);
		when(userRepository.save(ArgumentMatchers.any(User.class)))
				.thenThrow(new RuntimeException("Simulated DB Crash"));

		CustomException ex = assertThrows(CustomException.class, () -> userService.addUser(registerDto));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatus());
	}

	@Test
	@DisplayName("Test for Login - Success (Generates JWT)")
	void testVerify_Success() {
		UserLoginDto loginDto = new UserLoginDto();
		loginDto.setName("TestUser");
		loginDto.setPassword("pass123");

		Authentication authentication = mock(Authentication.class);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(authManager.authenticate(ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(jwtUtils.generateToken("TestUser")).thenReturn("fake-jwt-token");

		String token = userService.verify(loginDto);

		assertEquals("fake-jwt-token", token);
	}

	@Test
	@DisplayName("Test for Login - Failure (Bad Credentials returns null)")
	void testVerify_Failure() {
		UserLoginDto loginDto = new UserLoginDto();
		loginDto.setName("TestUser");
		loginDto.setPassword("wrongPass");

		when(authManager.authenticate(ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		String token = userService.verify(loginDto);

		assertNull(token);
	}

	@Test
	@DisplayName("Test for Deleting User - Success")
	void testDeleteUserByIdByAdmin_Success() {
		UserIdRequestDto request = new UserIdRequestDto();
		request.setId(1L);

		when(userRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

		UserResponseDto result = userService.deleteUserByIdByAdmin(request);

		assertNotNull(result);
		verify(userRepository, times(1)).deleteById(1L);
	}

	@Test
	@DisplayName("Test for Deleting User - Returns null if User not found")
	void testDeleteUserByIdByAdmin_NotFound() {
		UserIdRequestDto request = new UserIdRequestDto();
		request.setId(999L);

		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		UserResponseDto result = userService.deleteUserByIdByAdmin(request);

		assertNull(result);
		verify(userRepository, never()).deleteById(ArgumentMatchers.anyLong());
	}

	@Test
	@DisplayName("Test for Getting User Details - Owner fetching own data (Keeps Cart)")
	void testGetUserDetailsById_OwnerSuccess() {
		UserIdRequestDto request = new UserIdRequestDto();
		request.setId(1L);

		mockSecurityContext(mockCustomerLoggedIn);
		when(userRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

		UserResponseDto result = userService.getUserDetailsById(request);

		assertNotNull(result);
		assertNotNull(result.getCart(), "Owner should be allowed to see their own cart!");
	}

	@Test
	@DisplayName("Test for Getting User Details - Admin fetching other's data (Hides Cart)")
	void testGetUserDetailsById_AdminSuccess() {
		UserIdRequestDto request = new UserIdRequestDto();
		request.setId(1L);

		mockSecurityContext(mockAdminLoggedIn);
		when(userRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

		UserResponseDto result = userService.getUserDetailsById(request);

		assertNotNull(result);
		assertNull(result.getCart(), "Admin fetching a user should have the cart masked to null!");
	}

	@Test
	@DisplayName("Testing for Getting User Details - Access Denied for Customer spying on another")
	void testGetUserDetailsById_AccessDenied() {
		UserIdRequestDto request = new UserIdRequestDto();
		request.setId(2L); // Getting: User 2

		mockSecurityContext(mockCustomerLoggedIn); // Login: User 1

		assertThrows(AccessDeniedException.class, () -> userService.getUserDetailsById(request));
		verify(userRepository, never()).findById(ArgumentMatchers.anyLong());
	}

	@Test
	@DisplayName("Test for Getting User Details - User not found in DB throws exception")
	void testGetUserDetailsById_NotFound() {
		UserIdRequestDto request = new UserIdRequestDto();
		request.setId(1L);

		mockSecurityContext(mockCustomerLoggedIn);
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> userService.getUserDetailsById(request));
	}

	@Test
	@DisplayName("Test for Updating User Details - Success")
	void testUpdateUserDetailsById_Success() {
		UserRegisterDto updateDto = new UserRegisterDto();
		updateDto.setName("UpdatedName");

		when(userRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
		when(userRepository.save(ArgumentMatchers.any(User.class))).thenAnswer(passedArgs -> passedArgs.getArgument(0));

		UserResponseDto result = userService.updateUserDetailsById(1L, updateDto);

		assertNotNull(result);
		assertEquals("UpdatedName", result.getName());
		assertEquals("hashedPass123", mockCustomer.getPassword(), "Old password should be retained if not provided");
	}

	@Test
	@DisplayName("Test for Updating User Details - User not found throws exception")
	void testUpdateUserDetailsById_NotFound() {
		UserRegisterDto updateDto = new UserRegisterDto();
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(NoSuchElementException.class, () -> userService.updateUserDetailsById(99L, updateDto));
	}

	@Test
	@DisplayName("Test for Getting All Users - Strips carts from response for ADMIN")
	void testGetAllUsers() {
		when(userRepository.findAllWithCartAndOrders()).thenReturn(List.of(mockCustomer, mockAdmin));

		List<UserResponseDto> result = userService.getAllUsers();

		assertEquals(2, result.size());
		assertNull(result.get(0).getCart(), "Cart must be forcefully sanitized to null for all users in the list");
	}
}
