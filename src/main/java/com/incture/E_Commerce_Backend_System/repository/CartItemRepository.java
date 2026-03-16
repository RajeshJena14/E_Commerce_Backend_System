package com.incture.E_Commerce_Backend_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.incture.E_Commerce_Backend_System.entity.Cart;
import com.incture.E_Commerce_Backend_System.entity.CartItem;
import com.incture.E_Commerce_Backend_System.entity.Product;

/**
 * Repository for managing individual items within a shopping cart
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	
	/**
	 * Finds a specific cart item based on the parent cart and the product it contains
	 * Useful for checking if a product already exists in the cart before adding it
	 */
	public CartItem findByCartAndProduct(Cart cart, Product product);
}
