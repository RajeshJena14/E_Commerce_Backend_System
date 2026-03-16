package com.incture.E_Commerce_Backend_System.dto;


public class OrderItemsResponseDto {
	private long id;
	private long productId;
	private String productName;
	private String productCategory;
	private int quantity;
	private double price;
	
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
	public int getQuantity() {
		return quantity;
	}
	public double getPrice() {
		return price;
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
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	
//	@Override
//	public String toString() {
//		try {
//			return new ObjectMapper().writeValueAsString(this);
//		} catch (JsonProcessingException e) {
//			return "Error Converting to JSON...";
//		}
//	}
}
