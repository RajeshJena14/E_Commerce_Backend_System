package com.incture.E_Commerce_Backend_System.utils;


import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for managing JSON Web Tokens:
 * 		User Authentication
 * 		Handles token generation,
 * 		Claim extraction,
 * 		Validation for user authentication
 */
@Component
public class JWTUtils {
	
	// Secret key for JWT Signature
	private String SECRET_KEY = "V#f9N@2p$mR6wY7&aBvE*Nc5KjLqT4uQZ9x8YdG1h8M2sXn+O2";

	/**
	 * Generates a new JWT for an authenticated user
	 * Token Validity: 30 minutes
	 */
	public String generateToken(String name) {
		Map<String, Object> claims = new HashMap<String, Object>();

		return Jwts.builder().claims().add(claims).subject(name)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + (30 * 60 * 1000))) // token applicable for 30 minutes
				.and().signWith(getKey()).compact();
	}

	/**
	 * Generates the HMAC SHA key from the SECRET_KEY
	 */
	private SecretKey getKey() {
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
	}

	// Following Methods are only for Validation for user authentication
	public String extractUsername(String token) {
		// TODO Auto-generated method stub
		return extractClaim(token, Claims::getSubject);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
		// TODO Auto-generated method stub
		final Claims claims = extractAllClaims(token);
		return claimResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		// TODO Auto-generated method stub
		return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
	}

	public boolean validate(String token, UserDetails userDetails) {
		// TODO Auto-generated method stub
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	private boolean isTokenExpired(String token) {
		// TODO Auto-generated method stub
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		// TODO Auto-generated method stub
		return extractClaim(token, Claims::getExpiration);
	}
}
