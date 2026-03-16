package com.incture.E_Commerce_Backend_System;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = { "spring.mail.username=dummy@test.com", "spring.mail.password=dummyPassword" })
class ECommerceBackendSystemApplicationTests {

	@MockitoBean
	private JavaMailSender javaMailSender;

	@Test
	void contextLoads() {
	}

}
