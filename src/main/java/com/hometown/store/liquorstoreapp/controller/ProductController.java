package com.hometown.store.liquorstoreapp.controller;

import com.hometown.store.liquorstoreapp.dto.ProductDTO;
import com.hometown.store.liquorstoreapp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    // Get all products
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    // Get product by ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<ProductDTO> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Get products by category
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable String categoryName) {
        List<ProductDTO> products = productService.getProductsByCategory(categoryName);
        return ResponseEntity.ok(products);
    }
    
    // Get featured products
    @GetMapping("/featured")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts() {
        List<ProductDTO> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }
    
    // Search products
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String q) {
        List<ProductDTO> products = productService.searchProducts(q);
        return ResponseEntity.ok(products);
    }
    
    // Advanced search with filters and pagination
    @GetMapping("/search/advanced")
    public ResponseEntity<Page<ProductDTO>> searchProductsAdvanced(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Page<ProductDTO> products = productService.searchProductsWithFilters(
                category, type, region, minPrice, maxPrice, inStock, 
                page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(products);
    }
    
    // Create new product
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Update product
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        Optional<ProductDTO> updatedProduct = productService.updateProduct(id, productDTO);
        return updatedProduct.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Update product price
    @PatchMapping("/{id}/price")
    public ResponseEntity<Void> updatePrice(@PathVariable Long id, @RequestBody PriceUpdateRequest request) {
        boolean updated = productService.updatePrice(id, request.getPrice());
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    // Update stock status
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> updateStockStatus(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        boolean updated = productService.updateStockStatus(id, request.getInStock());
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    // Update featured status
    @PatchMapping("/{id}/featured")
    public ResponseEntity<Void> updateFeaturedStatus(@PathVariable Long id, @RequestBody FeaturedUpdateRequest request) {
        boolean updated = productService.updateFeaturedStatus(id, request.getFeatured());
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    // Delete product (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    // Get filter options
    @GetMapping("/filters")
    public ResponseEntity<ProductService.FilterOptionsDTO> getFilterOptions() {
        ProductService.FilterOptionsDTO options = productService.getFilterOptions();
        return ResponseEntity.ok(options);
    }
    
    // Get low stock products
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts() {
        List<ProductDTO> products = productService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }
    
    // Request DTOs for PATCH operations
    public static class PriceUpdateRequest {
        private BigDecimal price;
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
    
    public static class StockUpdateRequest {
        private Boolean inStock;
        
        public Boolean getInStock() {
            return inStock;
        }
        
        public void setInStock(Boolean inStock) {
            this.inStock = inStock;
        }
    }
    
    public static class FeaturedUpdateRequest {
        private Boolean featured;
        
        public Boolean getFeatured() {
            return featured;
        }
        
        public void setFeatured(Boolean featured) {
            this.featured = featured;
        }
    }
}