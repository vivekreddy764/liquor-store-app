package com.hometown.store.liquorstoreapp.service;

import com.hometown.store.liquorstoreapp.dto.ProductDTO;
import com.hometown.store.liquorstoreapp.entity.Category;
import com.hometown.store.liquorstoreapp.entity.Inventory;
import com.hometown.store.liquorstoreapp.entity.Product;
import com.hometown.store.liquorstoreapp.repository.CategoryRepository;
import com.hometown.store.liquorstoreapp.repository.InventoryRepository;
import com.hometown.store.liquorstoreapp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    // Get all active products
    public List<ProductDTO> getAllProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Get product by ID
    public Optional<ProductDTO> getProductById(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .map(this::convertToDTO);
    }
    
    // Get products by category
    public List<ProductDTO> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryNameAndActiveTrue(categoryName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Get featured products
    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Search products
    public List<ProductDTO> searchProducts(String searchTerm) {
        return productRepository.searchProducts(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Advanced search with filters and pagination
    public Page<ProductDTO> searchProductsWithFilters(
            String categoryName,
            String type,
            String region,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return productRepository.findProductsWithFilters(
                categoryName, type, region, minPrice, maxPrice, inStock, pageable)
                .map(this::convertToDTO);
    }
    
    // Create new product
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        
        // Set category if provided
        if (productDTO.getCategoryName() != null) {
            Optional<Category> category = categoryRepository.findByNameIgnoreCaseAndActiveTrue(productDTO.getCategoryName());
            category.ifPresent(product::setCategory);
        }
        
        Product savedProduct = productRepository.save(product);
        
        // Create inventory record
        Inventory inventory = new Inventory(savedProduct, productDTO.getTotalQuantity() != null ? productDTO.getTotalQuantity() : 0);
        inventoryRepository.save(inventory);
        
        return convertToDTO(savedProduct);
    }
    
    // Update product
    public Optional<ProductDTO> updateProduct(Long id, ProductDTO productDTO) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .map(product -> {
                    updateProductFields(product, productDTO);
                    
                    // Update category if provided
                    if (productDTO.getCategoryName() != null) {
                        Optional<Category> category = categoryRepository.findByNameIgnoreCaseAndActiveTrue(productDTO.getCategoryName());
                        category.ifPresent(product::setCategory);
                    }
                    
                    Product savedProduct = productRepository.save(product);
                    return convertToDTO(savedProduct);
                });
    }
    
    // Update product price
    public boolean updatePrice(Long id, BigDecimal newPrice) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent() && productOpt.get().getActive()) {
            productRepository.updatePrice(id, newPrice);
            return true;
        }
        return false;
    }
    
    // Update stock status
    public boolean updateStockStatus(Long id, Boolean inStock) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent() && productOpt.get().getActive()) {
            productRepository.updateStockStatus(id, inStock);
            return true;
        }
        return false;
    }
    
    // Update featured status
    public boolean updateFeaturedStatus(Long id, Boolean featured) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent() && productOpt.get().getActive()) {
            productRepository.updateFeaturedStatus(id, featured);
            return true;
        }
        return false;
    }
    
    // Soft delete product
    public boolean deleteProduct(Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent() && productOpt.get().getActive()) {
            productRepository.softDelete(id);
            return true;
        }
        return false;
    }
    
    // Get filter options
    public FilterOptionsDTO getFilterOptions() {
        FilterOptionsDTO options = new FilterOptionsDTO();
        options.setTypes(productRepository.findDistinctTypes());
        options.setRegions(productRepository.findDistinctRegions());
        return options;
    }
    
    // Get low stock products
    public List<ProductDTO> getLowStockProducts() {
        return productRepository.findLowStockProducts()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Convert entity to DTO
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setType(product.getType());
        dto.setRegion(product.getRegion());
        dto.setDistillery(product.getDistillery());
        dto.setVintage(product.getVintage());
        dto.setAge(product.getAge());
        dto.setAlcohol(product.getAlcohol());
        dto.setRating(product.getRating());
        dto.setReviews(product.getReviews());
        dto.setInStock(product.getInStock());
        dto.setFeatured(product.getFeatured());
        dto.setActive(product.getActive());
        dto.setPrimaryImageUrl(product.getPrimaryImageUrl());
        dto.setFeatures(product.getFeatures());
        dto.setImageUrls(product.getImageUrls());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        // Set category name
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        
        // Set inventory information
        if (product.getInventory() != null) {
            dto.setAvailableQuantity(product.getInventory().getAvailableQuantity());
            dto.setTotalQuantity(product.getInventory().getTotalQuantity());
            dto.setLowStockAlert(product.getInventory().getLowStockAlert());
        }
        
        return dto;
    }
    
    // Convert DTO to entity
    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        updateProductFields(product, dto);
        return product;
    }
    
    // Update product fields from DTO
    private void updateProductFields(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setType(dto.getType());
        product.setRegion(dto.getRegion());
        product.setDistillery(dto.getDistillery());
        product.setVintage(dto.getVintage());
        product.setAge(dto.getAge());
        product.setAlcohol(dto.getAlcohol());
        product.setRating(dto.getRating());
        product.setReviews(dto.getReviews());
        product.setInStock(dto.getInStock());
        product.setFeatured(dto.getFeatured());
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
        product.setPrimaryImageUrl(dto.getPrimaryImageUrl());
        product.setFeatures(dto.getFeatures());
        product.setImageUrls(dto.getImageUrls());
    }
    
    // Inner class for filter options
    public static class FilterOptionsDTO {
        private List<String> types;
        private List<String> regions;
        
        public List<String> getTypes() {
            return types;
        }
        
        public void setTypes(List<String> types) {
            this.types = types;
        }
        
        public List<String> getRegions() {
            return regions;
        }
        
        public void setRegions(List<String> regions) {
            this.regions = regions;
        }
    }
}