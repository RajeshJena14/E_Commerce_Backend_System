package com.incture.E_Commerce_Backend_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.incture.E_Commerce_Backend_System.entity.OrderItems;

/**
 * Repository for managing individual items within an order
 */
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {

}
