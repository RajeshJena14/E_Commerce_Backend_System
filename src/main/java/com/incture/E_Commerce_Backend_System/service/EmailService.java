package com.incture.E_Commerce_Backend_System.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.incture.E_Commerce_Backend_System.entity.EmailDetails;

/**
 * Concrete implementation of the EmailInterface
 * Utilizes JavaMailSender to dispatch emails asynchronously
 */
@Service
public class EmailService implements EmailInterface {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender javaMailSender;

	public EmailService(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	@Value("${spring.mail.username}")
	private String sender;

	/**
	 * Dispatches a simple text email on a separate background thread (@Async)
	 * Prevents email sending process from blocking the main application flow (e.g, Order Checkout)
	 */
	@Override
	@Async
	public void sendSimpleMail(EmailDetails emailDetails) {
		logger.debug("Attempting to send background email to: {}", emailDetails.getRecipient());
		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setFrom(sender);
			mailMessage.setTo(emailDetails.getRecipient());
			mailMessage.setText(emailDetails.getMsgBody());
			mailMessage.setSubject(emailDetails.getSubject());

			javaMailSender.send(mailMessage);
			logger.info("Successfully sent email to: {}", emailDetails.getRecipient());
		} catch (MailException e) {
			logger.error("Failed to send background email to {}. Error: {}", emailDetails.getRecipient(),
					e.getMessage());
		}
	}
}
