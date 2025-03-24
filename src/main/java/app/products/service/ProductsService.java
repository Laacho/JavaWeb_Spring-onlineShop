package app.products.service;


import app.exceptions.EntityNotFoundException;
import app.exceptions.ProductNameAlreadyExistsException;
import app.order_details.model.OrderDetails;
import app.order_details.service.OrderDetailsService;
import app.products.model.Product;
import app.products.repository.ProductsRepository;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.AddAProductRequest;
import app.web.dto.AdminSearchRequest;
import app.web.dto.EditProductDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final UserService userService;
    private final OrderDetailsService orderDetailsService;

    @Autowired
    public ProductsService(ProductsRepository productsRepository, UserService userService, OrderDetailsService orderDetailsService) {
        this.productsRepository = productsRepository;
        this.userService = userService;
        this.orderDetailsService = orderDetailsService;
    }

    public void addAProduct(AddAProductRequest productRequest) {
        checkForUniqueProductName(productRequest.getProductName());
        Product product = initProduct(productRequest);
        productsRepository.save(product);
    }

    public List<Product> findAllProductsDependingOnRole(UserRole role) {
        if (role == UserRole.ADMIN) {
            return productsRepository.findAllByOnDealFalse();
        }
        return productsRepository.findAllByAvailableTrueAndOnDealFalse();
    }

    public List<Product> getRecommendedProductsForUser(UUID userId) {
        User user = userService.getById(userId);
        //if the user is a new acount( no orders) give him the most ordered products from all users
        //otherswise give him the most ordered products of his orders
        if (user.getOrders().isEmpty()) {
            return orderDetailsService.getMostOrderedProducts();
        } else {
            return userService.getMostOrderedProducts(user.getOrders());
        }
    }

    public List<Product> search(AdminSearchRequest adminSearchRequest) {
        return productsRepository.findAllByNameContainingIgnoreCase(adminSearchRequest.getProductName());
    }

    public Product getById(UUID id) {
        return productsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }


    public List<Product> searchByName(String productName) {
        return productsRepository.findByNameContainingIgnoreCase(productName);
    }


    public void updateProduct(UUID id, EditProductDetails editProductDetails) {
        Product product = getById(id);
        product.setName(editProductDetails.getProductName());
        product.setPrice(editProductDetails.getPrice());
        product.setCategory(editProductDetails.getCategory());
        product.setQuantityPerUnit(editProductDetails.getQuantityPerUnit());
        product.setStockQuantity(editProductDetails.getStockQuantity());
        product.setDescription(editProductDetails.getDescription());
        product.setAvailable(editProductDetails.getAvailable());
        product.setPhoto(editProductDetails.getImage());

        productsRepository.save(product);
    }

    private Product initProduct(AddAProductRequest productRequest) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .name(productRequest.getProductName())
                .photo(productRequest.getProductImageURL())
                .price(productRequest.getPrice())
                .category(productRequest.getCategory())
                .quantityPerUnit(productRequest.getQuantityPerUnit())
                .stockQuantity(productRequest.getStockQuantity())
                .description(productRequest.getDescription())
                .available(true)
                .onDeal(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void checkForUniqueProductName(String productName) {
        Optional<Product> byName = productsRepository.findByName(productName);
        if (byName.isPresent()) {
            throw new ProductNameAlreadyExistsException("Product name already exists");
        }
    }

    public void reduceItemQuantity(List<OrderDetails> orderDetailsList) {
        for (OrderDetails orderDetails : orderDetailsList) {
            Product product = orderDetails.getProduct();
            int quantity = orderDetails.getQuantity();
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productsRepository.save(product);
        }
    }

    public void removeAllDeals() {
        productsRepository.removeAllDeals();
    }

    public List<Product> findAll() {
        return productsRepository.findAll();
    }

    public void saveAll(List<Product> products) {
        productsRepository.saveAll(products);
    }

    public List<Product> findAllProductsOnDeal() {
        return productsRepository.findAllByOnDealTrue();
    }
}
