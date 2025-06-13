package com.hometown.store.liquorstoreapp.repository;

import com.hometown.store.liquorstoreapp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find active categories
    List<Category> findByActiveTrueOrderByDisplayOrder();
    
    // Find by name
    Optional<Category> findByNameAndActiveTrue(String name);
    
    // Find by name (case insensitive)
    Optional<Category> findByNameIgnoreCaseAndActiveTrue(String name);
    
    // Check if category exists by name
    boolean existsByNameIgnoreCase(String name);
    
    // Get categories with product count
    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p " +
           "WHERE c.active = true AND (p.active = true OR p IS NULL) " +
           "GROUP BY c ORDER BY c.displayOrder")
    List<Object[]> findCategoriesWithProductCount();
}