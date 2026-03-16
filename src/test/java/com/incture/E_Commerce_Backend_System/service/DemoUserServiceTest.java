package com.incture.E_Commerce_Backend_System.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import com.incture.E_Commerce_Backend_System.entity.User;
import com.incture.E_Commerce_Backend_System.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DemoUserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Spy
	private ModelMapper modelMapper = new ModelMapper();

	@InjectMocks
	private DemoUserService demoUserService;

	private User mockUser;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing DemoUser Service...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested DemoUser Service...");
	}

	@BeforeEach
	void setUp() {
		mockUser = new User();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		mockUser.setName("SpringSecurityUser");
		mockUser.setEmail("security@test.com");
		mockUser.setPassword("encodedPass");
		mockUser.setRole("CUSTOMER");
	}

	@AfterEach
	void tearDown() throws Exception {
		mockUser = null;
	}

	@Test
	@DisplayName("Test for Loading User By Username - Success")
	void testLoadUserByUsername_Success() {
		when(userRepository.findByName("SpringSecurityUser")).thenReturn(mockUser);

		UserDetails result = demoUserService.loadUserByUsername("SpringSecurityUser");

		assertNotNull(result);
		assertEquals("SpringSecurityUser", result.getUsername());

		assertTrue(result.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER")));
	}

	@Test
	@DisplayName("Test for Loading User By Username - User Not Found")
	void testLoadUserByUsername_NotFound() {
		when(userRepository.findByName("GhostUser")).thenReturn(null);

		UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
				() -> demoUserService.loadUserByUsername("GhostUser"));

		assertTrue(ex.getMessage().contains("GhostUser"));
	}

}
