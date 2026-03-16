package com.incture.E_Commerce_Backend_System.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.incture.E_Commerce_Backend_System.entity.Orders;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Repository for managing user orders and checkout history
 */
@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
	
	public List<Orders> findByOrderDate(LocalDateTime orderDate);
	
	/**
	 * Fetches all orders for a specific user, including all items and associated products
	 * Used JOIN FETCH for optimizing performance and prevent N+1 problem
	 */
	@Query("SELECT o FROM Orders o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.user.id = :userId")
	public List<Orders> findByUserId(@Param("userId") Long user_id);
	
	/**
	 * Fetches a paginated list of orders for a specific user
	 * Used @EntityGraph as an alternative to JOIN FETCH for safe eager-load nested collections during pagination
	 */
	@EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
	public Page<Orders> findByUserId(@Param("userId") Long user_id, Pageable pageable);
}
