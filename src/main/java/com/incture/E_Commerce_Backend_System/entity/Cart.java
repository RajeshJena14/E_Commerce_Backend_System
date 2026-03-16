package com.incture.E_Commerce_Backend_System.entity;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart")
public class Cart {
	// Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@OneToOne
	@JoinColumn(name = "user_id")
	@JsonBackReference("user_cart")
	private User user;

	@Column(nullable = false)
	private double total_price;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JsonManagedReference("cart_cartItems")
	private List<CartItem> cartItems = new ArrayList<CartItem>();

	public long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public double getTotal_price() {
		return total_price;
	}

	public List<CartItem> getCartItems() {
		return cartItems;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setTotal_price(double total_price) {
		this.total_price = total_price;
	}

	public void setCartItems(List<CartItem> cartItems) {
		this.cartItems = cartItems;
	}

	public Cart(User user, double total_price, List<CartItem> cartItems) {
		this.user = user;
		this.total_price = total_price;
		this.cartItems = cartItems;
	}

	public Cart() {
	}

	@Override
	public String toString() {
		return "Cart [id=" + id + ", user=" + user + ", total_price=" + total_price + ", cartItems=" + cartItems + "]";
	}
}
