package com.incture.E_Commerce_Backend_System.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_items")
public class CartItem {
	// Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@ManyToOne
	@JoinColumn(name="cart_id")
	@JsonBackReference("cart_cartItems")
	private Cart cart;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="product_id")
	@JsonBackReference("product_cartItems")
	private Product product;
	
	private int quantity;

	public long getId() {
		return id;
	}

	public Cart getCart() {
		return cart;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public CartItem(Cart cart, Product product, int quantity) {
		this.cart = cart;
		this.product = product;
		this.quantity = quantity;
	}

	public CartItem() {
	}

	@Override
	public String toString() {
		return "CartItem [id=" + id + ", cart=" + cart + ", product=" + product + ", quantity=" + quantity + "]";
	}
}
