package com.incture.E_Commerce_Backend_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.incture.E_Commerce_Backend_System.entity.Cart;

/**
 * Repository for managing user shopping carts
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long>{
	
	/**
	 * Fetches the user's cart along with all associated cart items and products in a single query
	 * Used JOIN FETCH for optimizing database performance and prevent N+1 problem
	 */
	@Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.user.id = :userId")
	public Cart findByUserId(@Param("userId") Long user_id);
}
