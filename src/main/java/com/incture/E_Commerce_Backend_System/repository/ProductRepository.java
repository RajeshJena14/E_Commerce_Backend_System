package com.incture.E_Commerce_Backend_System.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.incture.E_Commerce_Backend_System.entity.Product;
import java.util.List;

/**
 * Repository for managing the product catalog
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	public Product findByName(String name);

	/**
	 * Retrieves a paginated list of products filtered by a specific category
	 */
	public Page<Product> findByCategory(String category, Pageable pageable);

	public List<Product> findByOrderByPriceAsc();

	public List<Product> findByOrderByPriceDesc();
}
