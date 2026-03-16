package com.incture.E_Commerce_Backend_System.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException{
	HttpStatus status;

	public CustomException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
	
}
