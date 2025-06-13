package com.hometown.store.liquorstoreapp.repository;

import com.hometown.store.liquorstoreapp.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    // Find by product ID
    Optional<Inventory> findByProductId(Long productId);
    
    // Find low stock items
    List<Inventory> findByLowStockAlertTrue();
    
    // Find out of stock items
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity = 0")
    List<Inventory> findOutOfStockItems();
    
    // Find items below minimum stock
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.minimumStock")
    List<Inventory> findBelowMinimumStock();
    
    // Update available quantity
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = :quantity, i.updatedAt = CURRENT_TIMESTAMP WHERE i.product.id = :productId")
    void updateAvailableQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    // Update total quantity
    @Modifying
    @Query("UPDATE Inventory i SET i.totalQuantity = :quantity, i.updatedAt = CURRENT_TIMESTAMP WHERE i.product.id = :productId")
    void updateTotalQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    // Reserve stock
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity - :quantity, " +
           "i.reservedQuantity = i.reservedQuantity + :quantity, i.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE i.product.id = :productId AND i.availableQuantity >= :quantity")
    int reserveStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    // Release reserved stock
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity + :quantity, " +
           "i.reservedQuantity = i.reservedQuantity - :quantity, i.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE i.product.id = :productId AND i.reservedQuantity >= :quantity")
    int releaseReservedStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    // Complete sale (remove from reserved and total)
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity, " +
           "i.totalQuantity = i.totalQuantity - :quantity, i.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE i.product.id = :productId AND i.reservedQuantity >= :quantity")
    int completeSale(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    // Restock inventory
    @Modifying
    @Query("UPDATE Inventory i SET i.totalQuantity = i.totalQuantity + :quantity, " +
           "i.availableQuantity = i.availableQuantity + :quantity, " +
           "i.lastRestockedAt = CURRENT_TIMESTAMP, i.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE i.product.id = :productId")
    void restockInventory(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    // Get total inventory value
    @Query("SELECT SUM(i.totalQuantity * p.price) FROM Inventory i JOIN i.product p WHERE p.active = true")
    Double getTotalInventoryValue();
    
    // Get inventory statistics
    @Query("SELECT COUNT(i), SUM(i.totalQuantity), SUM(i.availableQuantity), SUM(i.reservedQuantity) " +
           "FROM Inventory i JOIN i.product p WHERE p.active = true")
    Object[] getInventoryStatistics();
}