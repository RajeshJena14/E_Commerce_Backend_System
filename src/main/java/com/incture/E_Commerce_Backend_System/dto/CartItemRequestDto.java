package com.incture.E_Commerce_Backend_System.dto;

public class CartItemRequestDto {
	private long product_id;
	private int quantity;
	public long getProduct_id() {
		return product_id;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setProduct_id(long product_id) {
		this.product_id = product_id;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
