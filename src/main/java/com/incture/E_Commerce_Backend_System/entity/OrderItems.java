package com.incture.E_Commerce_Backend_System.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItems {
	// Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@ManyToOne
	@JoinColumn(name="order_id")
	@JsonBackReference("order_orderItems")
	private Orders order;
	
	@ManyToOne
	@JoinColumn(name="product_id")
	@JsonBackReference("product_orderItems")
	private Product product;
	
	private int quantity;
	
	private double price;

	public long getId() {
		return id;
	}

	public Orders getOrder() {
		return order;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getPrice() {
		return price;
	}

	public OrderItems(Orders order, Product product, int quantity, double price) {
		this.order = order;
		this.product = product;
		this.quantity = quantity;
		this.price = price;
	}

	public OrderItems() {
		
	}

	@Override
	public String toString() {
		return "OrderItems [id=" + id + ", order=" + order + ", product=" + product + ", quantity=" + quantity
				+ ", price=" + price + "]";
	}
}
