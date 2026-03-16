package com.incture.E_Commerce_Backend_System.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Orders {
	// Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne
	@JoinColumn(name="user_id")
	@JsonBackReference("user_orders")
	private User user;

	private double total_amount;

	@Column(nullable = false)
	private LocalDateTime orderDate;

	@Column(nullable = false)
	private String payment_status;

	@Column(nullable = false)
	private String order_status;
	
	@OneToMany(mappedBy="order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JsonManagedReference("order_orderItems")
	private List<OrderItems> orderItems = new ArrayList<OrderItems>();

	public long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public double getTotal_amount() {
		return total_amount;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public String getPayment_status() {
		return payment_status;
	}

	public String getOrder_status() {
		return order_status;
	}
	
	public List<OrderItems> getOrderItems() {
		return orderItems;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setTotal_amount(double total_amount) {
		this.total_amount = total_amount;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public void setPayment_status(String payment_status) {
		this.payment_status = payment_status;
	}

	public void setOrder_status(String order_status) {
		this.order_status = order_status;
	}

	public void setOrderItems(List<OrderItems> orderItems) {
		this.orderItems = orderItems;
	}

	public Orders(User user, double total_amount, LocalDateTime orderDate, String payment_status, String order_status,
			List<OrderItems> orderItems) {
		this.user = user;
		this.total_amount = total_amount;
		this.orderDate = orderDate;
		this.payment_status = payment_status;
		this.order_status = order_status;
		this.orderItems = orderItems;
	}

	public Orders() {
	}

	@Override
	public String toString() {
		return "Orders [id=" + id + ", user=" + user + ", total_amount=" + total_amount + ", orderDate=" + orderDate
				+ ", payment_status=" + payment_status + ", order_status=" + order_status + ", orderItems=" + orderItems
				+ "]";
	}

}
