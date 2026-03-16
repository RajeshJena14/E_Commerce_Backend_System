package com.incture.E_Commerce_Backend_System.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.incture.E_Commerce_Backend_System.service.DemoUserService;
import com.incture.E_Commerce_Backend_System.utils.JWTUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom security filter that intercepts incoming HTTP requests
 * Extracts and validates the JWT from the Authorization header to authenticate users statelessly
 */
@Component
public class JWTFilter extends OncePerRequestFilter {
	
	@Autowired
	private JWTUtils jwtUtils;
	
	@Autowired
	private DemoUserService demoUserService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		String authHeader = request.getHeader("Authorization");
		String token = null;
		String username = null;

		// Extract the token from the Bearer string
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			token = authHeader.substring(7);
			username = jwtUtils.extractUsername(token);
		}

		// If a username is found and the user is not yet authenticated in this context
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			UserDetails userDetails = demoUserService.loadUserByUsername(username);
			
			// Validate token and set the authentication context for Spring Security
			if (jwtUtils.validate(token, userDetails)) {
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		
		// Proceed with the rest of the security filter chain
		filterChain.doFilter(request, response);

	}
}
