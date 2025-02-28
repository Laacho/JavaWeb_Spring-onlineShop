package app.products.service;

import app.cloudinary.CloudinaryService;
import app.exceptions.DomainException;
import app.order_details.service.OrderDetailsService;
import app.products.model.Product;
import app.products.repository.ProductsRepository;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.AddAProductRequest;
import app.web.dto.AdminSearchRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final OrderDetailsService orderDetailsService;

    @Autowired
    public ProductsService(ProductsRepository productsRepository, CloudinaryService cloudinaryService, UserService userService, OrderDetailsService orderDetailsService) {
        this.productsRepository = productsRepository;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
        this.orderDetailsService = orderDetailsService;
    }

    public void addAproduct(AddAProductRequest productRequest)  {
        checkForUniqueProductName(productRequest.getProductName());
        CompletableFuture<String> stringCompletableFuture = uploadToCloud(productRequest.getProductImageURL());
        stringCompletableFuture.thenAcceptAsync(urlFromCloud  -> {
            Product product = initProduct(productRequest, urlFromCloud);
            productsRepository.save(product);
        });
    }

    public List<Product> findAllProductsDependingOnRole(UserRole role) {
        if(role==UserRole.ADMIN){
            return productsRepository.findAll();
        }
        return productsRepository.findAllByIsAvailable(true);
    }

    public List<Product> getRecommendedProductsForUser(UUID userId) {
        User user = userService.getById(userId);
        //if the user is a new acount( no orders) give him the most ordered products
        //otherswise give him the most ordered of his items
        if(user.getOrders().isEmpty()){
            return orderDetailsService.getMostOrderedProducts();
        }
        else{
            return userService.getMostOrderedProducts(user.getOrders());
        }
    }



    public List<Product> search( AdminSearchRequest adminSearchRequest) {
        return productsRepository.findAllByNameContainingIgnoreCase(adminSearchRequest.getProductName());
    }

    public Product getById(UUID id) {
        return productsRepository.findById(id).orElseThrow(() -> new DomainException("Product not found"));
    }

    private Product initProduct(AddAProductRequest productRequest, String urlFromCloud) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .name(productRequest.getProductName())
                .photo(urlFromCloud)
                .price(productRequest.getPrice())
                .category(productRequest.getCategory())
                .quantity(productRequest.getQuantity())
                .description(productRequest.getDescription())
                .isAvailable(true)
                .isOnDeal(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void checkForUniqueProductName(String productName) {
        Optional<Product> byName = productsRepository.findByName(productName);
        if (byName.isPresent()) {
            throw new DomainException("Product name already exists");
        }
    }

    private CompletableFuture<String> uploadToCloud(String productImageURL)  {
        return CompletableFuture.supplyAsync(()->{
            try {
                return cloudinaryService.uploadFromUrlToProducts(productImageURL);
            }catch (IOException e){
                return productImageURL;
            }
        });
    }
}
