package com.incture.E_Commerce_Backend_System.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.incture.E_Commerce_Backend_System.entity.EmailDetails;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private JavaMailSender javaMailSender;

	@InjectMocks
	private EmailService emailService;

	private EmailDetails mockEmailDetails;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("Testing Email Service...");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("Tested Email Service...");
	}

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(emailService, "sender", "store@timelessticks.com");

		mockEmailDetails = new EmailDetails();
		mockEmailDetails.setRecipient("customer@test.com");
		mockEmailDetails.setSubject("Order Confirmation");
		mockEmailDetails.setMsgBody("Your order has been placed successfully.");
	}

	@AfterEach
	void tearDown() {
		mockEmailDetails = null;
	}

	@Test
	@DisplayName("Test for Sending Email - Success")
	void testSendSimpleMail_Success() {
		emailService.sendSimpleMail(mockEmailDetails);

		// Assert using an ArgumentCaptor
		ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(javaMailSender, times(1)).send(messageCaptor.capture());

		SimpleMailMessage capturedMessage = messageCaptor.getValue();

		assertEquals("store@timelessticks.com", capturedMessage.getFrom());
		assertEquals("customer@test.com", capturedMessage.getTo()[0]);
		assertEquals("Order Confirmation", capturedMessage.getSubject());
		assertEquals("Your order has been placed successfully.", capturedMessage.getText());
	}

	@Test
	@DisplayName("Test for Sending Email - Fails Gracefully (MailException)")
	void testSendSimpleMail_Failure() {
		doThrow(new MailSendException("SMTP Server Down")).when(javaMailSender)
				.send(ArgumentMatchers.any(SimpleMailMessage.class));

		assertDoesNotThrow(() -> emailService.sendSimpleMail(mockEmailDetails));

		verify(javaMailSender, times(1)).send(ArgumentMatchers.any(SimpleMailMessage.class));
	}

}
