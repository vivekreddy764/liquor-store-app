package com.hometown.store.liquorstoreapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer totalQuantity = 0;
    
    @Column(nullable = false)
    private Integer availableQuantity = 0;
    
    @Column(nullable = false)
    private Integer reservedQuantity = 0;
    
    @Column(nullable = false)
    private Integer minimumStock = 5;
    
    @Column(nullable = false)
    private Boolean lowStockAlert = false;
    
    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        checkLowStock();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        checkLowStock();
    }
    
    private void checkLowStock() {
        this.lowStockAlert = this.availableQuantity <= this.minimumStock;
    }
    
    // Constructors
    public Inventory() {}
    
    public Inventory(Product product, Integer totalQuantity) {
        this.product = product;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
    }
    
    // Business methods
    public boolean reserveStock(int quantity) {
        if (availableQuantity >= quantity) {
            availableQuantity -= quantity;
            reservedQuantity += quantity;
            return true;
        }
        return false;
    }
    
    public void releaseReservedStock(int quantity) {
        if (reservedQuantity >= quantity) {
            reservedQuantity -= quantity;
            availableQuantity += quantity;
        }
    }
    
    public void sellStock(int quantity) {
        if (reservedQuantity >= quantity) {
            reservedQuantity -= quantity;
            totalQuantity -= quantity;
        }
    }
    
    public void restockInventory(int quantity) {
        totalQuantity += quantity;
        availableQuantity += quantity;
        lastRestockedAt = LocalDateTime.now();
    }
    
    public boolean isInStock() {
        return availableQuantity > 0;
    }
    
    public boolean isLowStock() {
        return availableQuantity <= minimumStock;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public Integer getTotalQuantity() {
        return totalQuantity;
    }
    
    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    
    public Integer getReservedQuantity() {
        return reservedQuantity;
    }
    
    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
    
    public Integer getMinimumStock() {
        return minimumStock;
    }
    
    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }
    
    public Boolean getLowStockAlert() {
        return lowStockAlert;
    }
    
    public void setLowStockAlert(Boolean lowStockAlert) {
        this.lowStockAlert = lowStockAlert;
    }
    
    public LocalDateTime getLastRestockedAt() {
        return lastRestockedAt;
    }
    
    public void setLastRestockedAt(LocalDateTime lastRestockedAt) {
        this.lastRestockedAt = lastRestockedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}