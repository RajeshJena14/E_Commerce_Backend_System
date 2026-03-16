package com.incture.E_Commerce_Backend_System.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class OrderResponseDto {
	private long id;
	
	private long userId;
	
	private double total_amount;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime orderDate;
	
	private List<OrderItemsResponseDto> orderItems = new ArrayList<OrderItemsResponseDto>();
	
	private String payment_status;
	
	private String order_status;
	
	public long getId() {
		return id;
	}
	public long getUserId() {
		return userId;
	}
	public double getTotal_amount() {
		return total_amount;
	}
	public LocalDateTime getOrderDate() {
		return orderDate;
	}
	public List<OrderItemsResponseDto> getOrderItems() {
		return orderItems;
	}
	public String getPayment_status() {
		return payment_status;
	}
	public String getOrder_status() {
		return order_status;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public void setTotal_amount(double total_amount) {
		this.total_amount = total_amount;
	}
	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}
	public void setOrderItems(List<OrderItemsResponseDto> orderItems) {
		this.orderItems = orderItems;
	}
	public void setPayment_status(String payment_status) {
		this.payment_status = payment_status;
	}
	public void setOrder_status(String order_status) {
		this.order_status = order_status;
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
