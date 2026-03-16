package com.incture.E_Commerce_Backend_System.dto;

import java.time.LocalDateTime;

public class OrdersRequestDto {
	private long id;
	
	private long userId;
	
	private String orderStatus;
	
	private LocalDateTime orderDate;

	public long getId() {
		return id;
	}

	public long getUserId() {
		return userId;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}
}
