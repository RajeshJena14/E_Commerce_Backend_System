package com.incture.E_Commerce_Backend_System.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global application configuration for ModelMapper (managed by Spring Application Context)
 */
@Configuration
public class AppConfig {
	
	/**
	 * A singleton instance of ModelMapper for object mapping (e.g., Entity <--> DTO)
	 */
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
