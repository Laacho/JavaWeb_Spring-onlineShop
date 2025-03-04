package app.shopping_cart.service;

import app.products.model.Product;
import app.products.service.ProductsService;
import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.repository.ShoppingCartRepository;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ShoppingCartService {
    private final UserService userService;
    private final ProductsService productsService;
    private final ShoppingCartRepository shoppingCartRepository;
    @Autowired
    public ShoppingCartService(UserService userService, ProductsService productsService, ShoppingCartRepository shoppingCartRepository) {
        this.userService = userService;
        this.productsService = productsService;
        this.shoppingCartRepository = shoppingCartRepository;
    }

    public boolean addProductToCart(UUID productId, UUID userID) {
        Product productToAdd = productsService.getById(productId);
        if(!productToAdd.isAvailable() || productToAdd.getStockQuantity()<=0){
            return false;
        }
        User user = userService.getById(userID);
        ShoppingCart shoppingCart = user.getShoppingCart();
        Map<Product, Integer> products = shoppingCart.getProducts();
        if(products.containsKey(productToAdd)) {
            products.put(productToAdd, products.get(productToAdd) + 1);
        }
        else {
            products.put(productToAdd, 1);
        }
        shoppingCart.setAddedAt(LocalDateTime.now());
        shoppingCartRepository.save(shoppingCart);
        return true;
    }

    public void removeProduct(UUID productId, UUID userId) {
        Product product = productsService.getById(productId);
        User user = userService.getById(userId);
        ShoppingCart shoppingCart = user.getShoppingCart();
        Map<Product, Integer> products = shoppingCart.getProducts();
        products.remove(product);
        shoppingCart.setProducts(products);
        shoppingCartRepository.save(shoppingCart);
    }

    public void empty(UUID userId) {
        User user = userService.getById(userId);
        ShoppingCart shoppingCart = user.getShoppingCart();
        shoppingCart.setProducts(new LinkedHashMap<>());
        shoppingCartRepository.save(shoppingCart);
    }
}
