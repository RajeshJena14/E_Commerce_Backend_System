package com.incture.E_Commerce_Backend_System.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
	// Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private Double price;

	@Column(nullable = false)
	private Integer stock;

	@Column(nullable = false)
	private String category;

	private String image_url;

	private Integer rating;
	
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JsonManagedReference("product_cartItems")
	private List<CartItem> cartItems = new ArrayList<CartItem>();
	
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JsonManagedReference("product_orderItems")
	private List<OrderItems> orderItems = new ArrayList<OrderItems>();

	public List<CartItem> getCartItems() {
		return cartItems;
	}

	public List<OrderItems> getOrderItems() {
		return orderItems;
	}

	public void setCartItems(List<CartItem> cartItems) {
		this.cartItems = cartItems;
	}

	public void setOrderItems(List<OrderItems> orderItems) {
		this.orderItems = orderItems;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Double getPrice() {
		return price;
	}

	public Integer getStock() {
		return stock;
	}

	public String getCategory() {
		return category;
	}

	public String getImage_url() {
		return image_url;
	}

	public Integer getRating() {
		return rating;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public Product(String name, String description, Double price, Integer stock, String category, String image_url,
			Integer rating, List<CartItem> cartItems, List<OrderItems> orderItems) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.category = category;
		this.image_url = image_url;
		this.rating = rating;
		this.cartItems = cartItems;
		this.orderItems = orderItems;
	}

	public Product() {

	}

	@Override
	public String toString() {
		return "Product [id=" + id + ", name=" + name + ", description=" + description + ", price=" + price + ", stock="
				+ stock + ", category=" + category + ", image_url=" + image_url + ", rating=" + rating + ", cartItems="
				+ cartItems + ", orderItems=" + orderItems + "]";
	}
}
