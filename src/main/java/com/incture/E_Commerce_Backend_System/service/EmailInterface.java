package com.incture.E_Commerce_Backend_System.service;

import com.incture.E_Commerce_Backend_System.entity.EmailDetails;

/**
 * Defines the contract for email notification services within the application
 */
public interface EmailInterface {
	
	/**
	 * Sends a simple text-based email without attachments
	 * EmailDetails: recipient, subject, and body of the email
	 */
	void sendSimpleMail(EmailDetails details);
}
