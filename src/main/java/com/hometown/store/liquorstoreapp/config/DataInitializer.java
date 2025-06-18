package com.hometown.store.liquorstoreapp.config;

import com.hometown.store.liquorstoreapp.entity.Category;
import com.hometown.store.liquorstoreapp.entity.Product;
import com.hometown.store.liquorstoreapp.repository.CategoryRepository;
import com.hometown.store.liquorstoreapp.repository.ProductRepository;
import com.hometown.store.liquorstoreapp.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final AuthService authService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    
    public DataInitializer(AuthService authService, CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.authService = authService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin user
        authService.createAdminUser("admin", "admin@liquorstore.com", "Admin@123456");
        System.out.println("Default admin user created: admin / Admin@123456");
        
        // Initialize sample data if no products exist
        if (productRepository.count() == 0) {
            initializeSampleData();
            System.out.println("Sample products initialized for search testing");
        }
    }
    
    private void initializeSampleData() {
        // Create categories
        Category wineCategory = createCategory("Wine", "Premium wines from around the world", "🍷", 1);
        Category spiritsCategory = createCategory("Spirits", "Premium spirits and liquors", "🥃", 2);
        Category beerCategory = createCategory("Beer", "Craft and premium beers", "🍺", 3);
        
        // Create wine products
        createProduct("Château Margaux 2010", "Exceptional Bordeaux wine from one of the most prestigious estates", 
                     new BigDecimal("899.99"), new BigDecimal("999.99"), "Red Wine", "Bordeaux, France", 
                     "Château Margaux", "2010", "", "13.5%", 4.8, 156, wineCategory);
        
        createProduct("Dom Pérignon Vintage 2012", "Legendary champagne with exceptional elegance and finesse", 
                     new BigDecimal("249.99"), new BigDecimal("299.99"), "Champagne", "Champagne, France", 
                     "Dom Pérignon", "2012", "", "12.5%", 4.7, 89, wineCategory);
        
        createProduct("Caymus Cabernet Sauvignon", "Rich and bold Napa Valley Cabernet with dark fruit flavors", 
                     new BigDecimal("79.99"), new BigDecimal("89.99"), "Red Wine", "Napa Valley, California", 
                     "Caymus Vineyards", "2020", "", "14.5%", 4.5, 234, wineCategory);
        
        createProduct("Cloudy Bay Sauvignon Blanc", "Crisp and refreshing New Zealand Sauvignon Blanc", 
                     new BigDecimal("24.99"), new BigDecimal("29.99"), "White Wine", "Marlborough, New Zealand", 
                     "Cloudy Bay", "2022", "", "13%", 4.3, 187, wineCategory);
        
        // Create spirits products
        createProduct("Macallan 18 Year Old", "Exceptional single malt Scotch whisky aged in sherry oak casks", 
                     new BigDecimal("499.99"), new BigDecimal("549.99"), "Scotch Whisky", "Speyside, Scotland", 
                     "The Macallan", "", "18 Years", "43%", 4.9, 312, spiritsCategory);
        
        createProduct("Hennessy XO Cognac", "Premium cognac with rich, complex flavors and smooth finish", 
                     new BigDecimal("199.99"), new BigDecimal("229.99"), "Cognac", "Cognac, France", 
                     "Hennessy", "", "XO", "40%", 4.6, 156, spiritsCategory);
        
        createProduct("Blanton's Single Barrel Bourbon", "Award-winning single barrel bourbon with vanilla and caramel notes", 
                     new BigDecimal("159.99"), new BigDecimal("179.99"), "Bourbon", "Kentucky, USA", 
                     "Buffalo Trace", "", "", "46.5%", 4.7, 298, spiritsCategory);
        
        createProduct("Grey Goose Vodka", "Ultra-premium French vodka with unparalleled smoothness", 
                     new BigDecimal("49.99"), new BigDecimal("59.99"), "Vodka", "France", 
                     "Grey Goose", "", "", "40%", 4.4, 432, spiritsCategory);
        
        createProduct("Clase Azul Reposado Tequila", "Handcrafted tequila aged in American whiskey barrels", 
                     new BigDecimal("129.99"), new BigDecimal("149.99"), "Tequila", "Jalisco, Mexico", 
                     "Clase Azul", "", "Reposado", "40%", 4.6, 176, spiritsCategory);
        
        createProduct("Johnnie Walker Blue Label", "Rare and exceptional blended Scotch whisky", 
                     new BigDecimal("189.99"), new BigDecimal("219.99"), "Blended Scotch", "Scotland", 
                     "Johnnie Walker", "", "", "40%", 4.5, 267, spiritsCategory);
        
        // Create beer products
        createProduct("Samuel Adams Boston Lager", "Classic American craft beer with rich malt flavor", 
                     new BigDecimal("12.99"), new BigDecimal("14.99"), "Lager", "Boston, Massachusetts", 
                     "Samuel Adams", "", "", "4.9%", 4.2, 543, beerCategory);
        
        createProduct("Sierra Nevada Pale Ale", "Pioneering American pale ale with citrusy hop character", 
                     new BigDecimal("11.99"), new BigDecimal("13.99"), "Pale Ale", "California, USA", 
                     "Sierra Nevada", "", "", "5.6%", 4.3, 678, beerCategory);
    }
    
    private Category createCategory(String name, String description, String icon, Integer displayOrder) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setIcon(icon);
        category.setDisplayOrder(displayOrder);
        category.setActive(true);
        return categoryRepository.save(category);
    }
    
    private void createProduct(String name, String description, BigDecimal price, BigDecimal originalPrice,
                              String type, String region, String distillery, String vintage, String age,
                              String alcohol, Double rating, Integer reviews, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        product.setType(type);
        product.setRegion(region);
        product.setDistillery(distillery);
        product.setVintage(vintage);
        product.setAge(age);
        product.setAlcohol(alcohol);
        product.setRating(rating);
        product.setReviews(reviews);
        product.setCategory(category);
        product.setInStock(true);
        product.setFeatured(false);
        product.setActive(true);
        productRepository.save(product);
    }
}