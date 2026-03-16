package com.incture.E_Commerce_Backend_System.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductResponseDTO {
	private long id;

	private String name;

	private String description;

	private Double price;

	private Integer stock;

	private String category;

	private String image_url;

	private Integer rating;

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

	public void setId(long id) {
		this.id = id;
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

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return "Error Converting to JSON...";
		}
	}
}
