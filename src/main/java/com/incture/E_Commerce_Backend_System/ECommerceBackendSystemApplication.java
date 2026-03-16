package com.incture.E_Commerce_Backend_System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ECommerceBackendSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceBackendSystemApplication.class, args);
	}

}
