package com.incture.E_Commerce_Backend_System.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.incture.E_Commerce_Backend_System.filter.JWTFilter;

/**
 * Security Configuration for the application (Authentication & Authorization rules)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	public static final String[] SWAGGER_URL = {
			"/v3/api-docs/**",
			"/swagger-ui/**",
			"/swagger-ui.html",
			"/error"
	};

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JWTFilter jwtFilter;

	/**
	 * Configures "Authentication Provider" using DAO and BCryptPasswordEncoding
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
		return provider;
	}

	/**
	 * Exposing "Authentication Manager" bean during authentication / login
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * "Security Filter Chain" definition (Endpoint Access Rules, Stateless Session Management, and Exception handling)
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						// Public Endpoints
						.requestMatchers("/api/users/register", "/api/users/login").permitAll()
						.requestMatchers(SWAGGER_URL).permitAll()
						
						// Endpoints strictly for ADMIN
						.requestMatchers(HttpMethod.DELETE, "/api/users/*", "/api/products/*").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/users/", "/api/users").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/products/", "/api/products").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/products/*", "/api/orders/*/status").hasRole("ADMIN")
						
						// Endpoints strictly for CUSTOMER
						.requestMatchers("/api/cart/**", "/api/orders/checkout", "/api/orders/history").hasRole("CUSTOMER")
						
						// Authentication required for all other Requests
						.anyRequest().authenticated())
				
				// Custom handler for unauthorized access attempts
				.exceptionHandling(e -> e.accessDeniedHandler((request, response, accessDeniedException) -> {
					response.setStatus(403);
					response.getWriter().write("Access Denied: You do not have permission to perform this action...");
				}))
				
				.httpBasic(Customizer.withDefaults())
				
				// Stateless sessions (JWT driven)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				
				// Custom JWT filter injected before the standard Authentication filter
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
	}
}
