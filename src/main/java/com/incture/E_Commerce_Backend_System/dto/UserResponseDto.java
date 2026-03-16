package com.incture.E_Commerce_Backend_System.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) 
@JsonIgnoreProperties({"authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "username"})
public class UserResponseDto implements UserDetails {

	private long id;
	private String name;
	private String email;
	private String password;
	private String role;
	private CartResponseDto cart;
	private List<OrderResponseDto> orders = new ArrayList<OrderResponseDto>();

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getRole() {
		return role;
	}

	public CartResponseDto getCart() {
		return cart;
	}

	public List<OrderResponseDto> getOrders() {
		return orders;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setCart(CartResponseDto cart) {
		this.cart = cart;
	}

	public void setOrders(List<OrderResponseDto> orders) {
		this.orders = orders;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		if (this.getRole().equals("ADMIN")) {
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		} else {
			authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
		}
		return authorities;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.getName();
	}
}
