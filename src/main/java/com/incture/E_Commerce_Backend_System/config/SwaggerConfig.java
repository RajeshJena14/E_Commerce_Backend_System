package com.incture.E_Commerce_Backend_System.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
	
	// Swagger UI for including Authorize button
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
						.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
						.components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
						.info(new Info().title("ECommerce Application APIs").version("1.0").description("By Rajesh Kumar Jena"))
//						.servers(List.of(new Server().url("http://localhost:8080").description("live")));
						.servers(List.of(new Server().url("http://localhost:8082").description("live")));
	}

	private SecurityScheme createAPIKeyScheme() {
		return new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.bearerFormat("JWT")
						.scheme("bearer")
						.in(SecurityScheme.In.HEADER)
						.name("Authorization");
	}
}
