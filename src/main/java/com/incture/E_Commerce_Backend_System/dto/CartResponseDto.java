package com.incture.E_Commerce_Backend_System.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CartResponseDto {
	private long id;
	private long userId;
	private double total_price;
	private List<CartItemResponseDto> cartItems = new ArrayList<CartItemResponseDto>();

	public long getId() {
		return id;
	}

	public long getUserId() {
		return userId;
	}

	public double getTotal_price() {
		return total_price;
	}

	public List<CartItemResponseDto> getCartItems() {
		return cartItems;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setUserId(long user_id) {
		this.userId = user_id;
	}

	public void setTotal_price(double total_price) {
		this.total_price = total_price;
	}

	public void setCartItems(List<CartItemResponseDto> cartItems) {
		this.cartItems = cartItems;
	}

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return "Error Converting to JSON...";
		}
	}
}
