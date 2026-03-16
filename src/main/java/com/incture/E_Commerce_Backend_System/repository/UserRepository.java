package com.incture.E_Commerce_Backend_System.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.incture.E_Commerce_Backend_System.entity.User;

/**
 * Repository for managing user accounts, authentication credentials, and user data
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	public User findByName(String name);
	
	/**
	 * Efficiently checks if an email is already registered without pulling the entire User entity into memory
	 */
	public boolean existsByEmail(String email);
	
	/**
	 * Fetches all users along with their associated carts and orders in a single optimized query
	 * DISTINCT keyword ensures no duplicate user rows are returned due to the JOIN FETCH
	 */
	@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.cart LEFT JOIN FETCH u.orders")
    public List<User> findAllWithCartAndOrders();
}
