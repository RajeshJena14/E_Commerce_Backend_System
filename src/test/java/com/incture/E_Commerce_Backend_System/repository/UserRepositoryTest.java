package com.incture.E_Commerce_Backend_System.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
//import org.springframework.boot.test.context.SpringBootTest;

import com.incture.E_Commerce_Backend_System.entity.User;

//@SpringBootTest
@DataJpaTest
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing User Repository...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested User Repository...");
	}

	@BeforeEach
	void setUp() throws Exception {
		User mockUser1 = new User();
		mockUser1.setName("ABC");
		mockUser1.setEmail("abc@gmail.com");
		mockUser1.setPassword("securePassword");
		mockUser1.setRole("CUSTOMER");

		User adminMockUser = new User();
		adminMockUser.setName("XYZ");
		adminMockUser.setEmail("xyz@gmail.com");
		adminMockUser.setPassword("admin789");
		adminMockUser.setRole("ADMIN");

		userRepository.saveAll(List.of(mockUser1, adminMockUser));
	}

	@AfterEach
	void tearDown() throws Exception {
		userRepository.deleteAll();
	}

	@DisplayName("Test for finding single user by Name")
	@Test
	void testFindByName() {
		User user1 = userRepository.findByName("ABC");
		User user2 = userRepository.findByName("PQR");

		assertNotNull(user1);
//		assertNotNull(user2);
		assertNull(user2);
	}

	@DisplayName("Test for finding multiple user by Name")
	@ParameterizedTest
//	@CsvSource({"ABC","MNO","XYZ"})
	@CsvSource({ "ABC", "XYZ" })
	void testFindByName_Parameterized(String name) {
		User user = userRepository.findByName(name);
		assertNotNull(user);
	}

	@DisplayName("Test for single Email existence")
	@Test
	void testExistsByEmail() {
		boolean exists = userRepository.existsByEmail("abc@gmail.com");
		boolean doesNotExist = userRepository.existsByEmail("test@gmail.com");

		assertTrue(exists);
//		assertTrue(doesNotExist, "Email: \"test@gmail.com\" does not exists");
		assertFalse(doesNotExist);
	}

	@DisplayName("Test for multiple Email existence")
	@ParameterizedTest
//	@CsvSource({"abc@gmail.com","xyz@gmail.com", "test@gmail.com"})
	@CsvSource({ "abc@gmail.com", "xyz@gmail.com" })
	void testExistsByEmail_Parameterized(String email) {
		boolean exists = userRepository.existsByEmail(email);
		assertTrue(exists, "Email: \"" + email + "\" does not exists");
	}

	@DisplayName("Test for finding all users")
	@Test
	void testFindAllWithCartAndOrders() {
		List<User> allUsers = userRepository.findAllWithCartAndOrders();
		assertNotNull(allUsers);
		assertFalse(allUsers.isEmpty());
		assertEquals("ABC", allUsers.get(0).getName(),
				"Actual name \"" + allUsers.get(0).getName() + "\" does not match with Expected name \"ABC\"");
		assertEquals("xyz@gmail.com", allUsers.get(1).getEmail(), "Actual email \"" + allUsers.get(1).getEmail()
				+ "\" does not match with Expected email \"xyz@gmail.com\"");
	}

}
