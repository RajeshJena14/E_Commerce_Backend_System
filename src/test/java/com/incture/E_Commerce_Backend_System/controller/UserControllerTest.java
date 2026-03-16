package com.incture.E_Commerce_Backend_System.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incture.E_Commerce_Backend_System.dto.UserLoginDto;
import com.incture.E_Commerce_Backend_System.dto.UserRegisterDto;
import com.incture.E_Commerce_Backend_System.dto.UserResponseDto;
import com.incture.E_Commerce_Backend_System.entity.User;
import com.incture.E_Commerce_Backend_System.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@Mock
	private UserService userService;

	@Spy
	private ModelMapper modelMapper = new ModelMapper();

	@InjectMocks
	private UserController userController;

	private User mockUser;
	private UserResponseDto mockResponseDto;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing User Controller...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested User Controller...");
	}

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
		objectMapper = new ObjectMapper();

		mockUser = new User();
		mockUser.setName("PostmanTester");
		mockUser.setEmail("tester@postman.com");
		mockUser.setPassword("securePass123");
		mockUser.setRole("CUSTOMER");

		mockResponseDto = new UserResponseDto();
		mockResponseDto.setId(1L);
		mockResponseDto.setName("PostmanTester");
		mockResponseDto.setRole("CUSTOMER");
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
		mockUser = null;
		mockResponseDto = null;
	}

	@Test
	@DisplayName("POST /register - Success (201 Created)")
	void testRegisterUser_Success() throws Exception {
		when(userService.addUser(ArgumentMatchers.any(UserRegisterDto.class))).thenReturn(mockResponseDto);

		mockMvc
				.perform(post("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mockUser)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("PostmanTester"))
			.andExpect(jsonPath("$.role").value("CUSTOMER"));
	}

	@Test
	@DisplayName("POST /register - Fails due to Empty Name (IllegalArgumentException)")
	void testRegisterUser_EmptyName() throws Exception {
		mockUser.setName("");

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc
					.perform(post("/api/users/register")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(mockUser)));
		});

		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("Name cannot be empty"));
	}
	
	@Test
	@DisplayName("POST /register - Business Logic: Defaults empty role to CUSTOMER")
	void testRegisterUser_DefaultsToCustomerRole() throws Exception {
		mockUser.setRole("");

		ArgumentCaptor<UserRegisterDto> dtoCaptor = ArgumentCaptor.forClass(UserRegisterDto.class);
		when(userService.addUser(dtoCaptor.capture())).thenReturn(mockResponseDto);

		mockMvc
				.perform(post("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mockUser)))
			.andExpect(status().isCreated());

		assertEquals("CUSTOMER", dtoCaptor.getValue().getRole());
	}

	@Test
	@DisplayName("POST /register - Business Logic: Converts role to UPPERCASE")
	void testRegisterUser_ConvertsRoleToUppercase() throws Exception {
		mockUser.setRole("admin");

		ArgumentCaptor<UserRegisterDto> dtoCaptor = ArgumentCaptor.forClass(UserRegisterDto.class);
		when(userService.addUser(dtoCaptor.capture())).thenReturn(mockResponseDto);

		mockMvc
				.perform(post("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mockUser)))
			.andExpect(status().isCreated());

		assertEquals("ADMIN", dtoCaptor.getValue().getRole());
	}

	@Test
	@DisplayName("POST /register - Business Logic: Encodes plaintext password using BcryptEncoding")
	void testRegisterUser_EncodesPassword() throws Exception {
		mockUser.setPassword("mySecretPass");

		ArgumentCaptor<UserRegisterDto> dtoCaptor = ArgumentCaptor.forClass(UserRegisterDto.class);
		when(userService.addUser(dtoCaptor.capture())).thenReturn(mockResponseDto);

		mockMvc
				.perform(post("/api/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mockUser)))
			.andExpect(status().isCreated());

		String capturedPassword = dtoCaptor.getValue().getPassword();

		assertNotEquals("mySecretPass", capturedPassword);
		assertTrue(capturedPassword.startsWith("$2a$"));
	}

	@Test
	@DisplayName("POST /login - Success (200 OK & JWT in Header)")
	void testLogin_Success() throws Exception {
		when(userService.verify(ArgumentMatchers.any(UserLoginDto.class))).thenReturn("fake-jwt-token-12345");

		mockMvc
				.perform(post("/api/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mockUser)))
			.andExpect(status().isOk())
			.andExpect(header().string("Authorization", "Bearer fake-jwt-token-12345"))
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Login Successful")));
	}

	@Test
	@DisplayName("POST /login - Fails due to Empty Password")
	void testLogin_EmptyPassword() throws Exception {
		mockUser.setPassword("");

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc
					.perform(post("/api/users/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(mockUser)));
		});

		assertTrue(exception.getCause() instanceof IllegalArgumentException);
	}

	@Test
	@DisplayName("DELETE /{id} - Success (204 No Content)")
	void testDeleteUserOnlyByAdmin_Success() throws Exception {
		when(userService.deleteUserByIdByAdmin(ArgumentMatchers.any())).thenReturn(mockResponseDto);

		mockMvc
				.perform(delete("/api/users/1"))
			.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("DELETE /{id} - Fails if User Not Found")
	void testDeleteUserOnlyByAdmin_NotFound() throws Exception {
		when(userService.deleteUserByIdByAdmin(ArgumentMatchers.any())).thenReturn(null);

		Exception exception = assertThrows(Exception.class, () -> {
			mockMvc.perform(delete("/api/users/99"));
		});

		assertTrue(exception.getCause() instanceof UsernameNotFoundException);
	}

	@Test
	@DisplayName("GET /{id} - Success (Returns User JSON)")
	void testGetUserDetails_Success() throws Exception {
		when(userService.getUserDetailsById(ArgumentMatchers.any())).thenReturn(mockResponseDto);

		mockMvc
				.perform(get("/api/users/1")).andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("PostmanTester"))
			.andExpect(jsonPath("$.role").value("CUSTOMER"));
	}

	@Test
	@DisplayName("GET / - Success (Returns List of Users JSON)")
	void testGetAllUserDetails_Success() throws Exception {
		when(userService.getAllUsers()).thenReturn(List.of(mockResponseDto));

		mockMvc
				.perform(get("/api/users/"))
			.andExpect(status().isOk())	
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].name").value("PostmanTester"));
	}

	@Test
	@DisplayName("PUT /{id} - Success (Returns Updated User)")
	void testUpdateUserDetails_Success() throws Exception {
		mockResponseDto.setName("UpdatedTester");
		when(userService.updateUserDetailsById(ArgumentMatchers.anyLong(), ArgumentMatchers.any(UserRegisterDto.class))).thenReturn(mockResponseDto);

		mockMvc
				.perform(put("/api/users/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mockUser)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("UpdatedTester"))
			.andExpect(jsonPath("$.role").value("CUSTOMER"));
	}


	@Test
	@DisplayName("PUT /{id} - Business Logic: Encodes new password if provided")
	void testUpdateUserDetails_EncodesNewPassword() throws Exception {
		mockUser.setPassword("newSecretPass");

		ArgumentCaptor<UserRegisterDto> dtoCaptor = ArgumentCaptor.forClass(UserRegisterDto.class);
		when(userService.updateUserDetailsById(ArgumentMatchers.anyLong(), dtoCaptor.capture())).thenReturn(mockResponseDto);

		mockMvc
				.perform(put("/api/users/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(mockUser)))
			.andExpect(status().isOk());

		String capturedPassword = dtoCaptor.getValue().getPassword();

		assertNotEquals("newSecretPass", capturedPassword);
		assertTrue(capturedPassword.startsWith("$2a$"));
	}
}