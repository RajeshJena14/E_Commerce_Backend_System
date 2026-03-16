package com.incture.E_Commerce_Backend_System.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CartItemResponseDto {
	private long id;
	private long productId;
	private String productName;
	private String productCategory;
	private double productPrice;
	private int quantity;

	public long getId() {
		return id;
	}

	public long getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public String getProductCategory() {
		return productCategory;
	}

	public double getProductPrice() {
		return productPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}

	public void setProductPrice(double productPrice) {
		this.productPrice = productPrice;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
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
