package com.hometown.store.liquorstoreapp.repository;

import com.hometown.store.liquorstoreapp.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find active products
    List<Product> findByActiveTrue();
    
    // Find products by category
    List<Product> findByCategoryNameAndActiveTrue(String categoryName);
    
    // Find featured products
    List<Product> findByFeaturedTrueAndActiveTrue();
    
    // Find products in stock
    List<Product> findByInStockTrueAndActiveTrue();
    
    // Find products by type
    List<Product> findByTypeAndActiveTrue(String type);
    
    // Find products by region
    List<Product> findByRegionAndActiveTrue(String region);
    
    // Find products by price range
    List<Product> findByPriceBetweenAndActiveTrue(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Search products by name or description
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.distillery) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);
    
    // Search with pagination
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.distillery) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Advanced search with filters
    @Query("SELECT p FROM Product p WHERE p.active = true " +
           "AND (:categoryName IS NULL OR p.category.name = :categoryName) " +
           "AND (:type IS NULL OR p.type = :type) " +
           "AND (:region IS NULL OR p.region = :region) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:inStock IS NULL OR p.inStock = :inStock)")
    Page<Product> findProductsWithFilters(
            @Param("categoryName") String categoryName,
            @Param("type") String type,
            @Param("region") String region,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable);
    
    // Update price
    @Modifying
    @Query("UPDATE Product p SET p.price = :price, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void updatePrice(@Param("id") Long id, @Param("price") BigDecimal price);
    
    // Update stock status
    @Modifying
    @Query("UPDATE Product p SET p.inStock = :inStock, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void updateStockStatus(@Param("id") Long id, @Param("inStock") Boolean inStock);
    
    // Set featured status
    @Modifying
    @Query("UPDATE Product p SET p.featured = :featured, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void updateFeaturedStatus(@Param("id") Long id, @Param("featured") Boolean featured);
    
    // Soft delete (deactivate)
    @Modifying
    @Query("UPDATE Product p SET p.active = false, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void softDelete(@Param("id") Long id);
    
    // Get distinct types
    @Query("SELECT DISTINCT p.type FROM Product p WHERE p.active = true ORDER BY p.type")
    List<String> findDistinctTypes();
    
    // Get distinct regions
    @Query("SELECT DISTINCT p.region FROM Product p WHERE p.active = true ORDER BY p.region")
    List<String> findDistinctRegions();
    
    // Get low stock products
    @Query("SELECT p FROM Product p JOIN p.inventory i WHERE p.active = true AND i.lowStockAlert = true")
    List<Product> findLowStockProducts();
    
    // Count products by category
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.name = :categoryName AND p.active = true")
    Long countByCategory(@Param("categoryName") String categoryName);
    
    // Find by name (case insensitive)
    Optional<Product> findByNameIgnoreCaseAndActiveTrue(String name);
    
    // Get top rated products
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.rating IS NOT NULL ORDER BY p.rating DESC")
    Page<Product> findTopRatedProducts(Pageable pageable);
    
    // Get newest products
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdAt DESC")
    Page<Product> findNewestProducts(Pageable pageable);
}