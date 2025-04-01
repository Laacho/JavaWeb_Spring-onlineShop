package app.unit;

import app.orders.service.OrderService;
import app.products.model.Product;
import app.products.service.ProductsService;
import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.repository.ShoppingCartRepository;
import app.shopping_cart.service.ShoppingCartService;
import app.user.model.User;
import app.user.service.UserService;
import app.voucher.service.VoucherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceUTest {

    @Mock
    private UserService userService;
    @Mock
    private ProductsService productsService;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private VoucherService voucherService;
    @Mock
    private OrderService orderService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    private UUID userId;
    private UUID productId;
    private User user;
    private Product product;
    private ShoppingCart shoppingCart;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        product = Product.builder().id(productId).build();

        Map<Product, Integer> products = new HashMap<>();
        products.put(product, 2);
        shoppingCart = ShoppingCart.builder().products(products).build();


        user = User.builder()
                .id(userId)
                .shoppingCart(shoppingCart)
                .build();

        when(userService.getById(userId)).thenReturn(user);
        when(productsService.getById(productId)).thenReturn(product);
    }

    @Test
    void testDecrementItemQuantity_ShouldDecrementSuccessfully() {

        shoppingCartService.decrementItemQuantity(productId, userId);


        int newQuantity = user.getShoppingCart().getProducts().get(product);
        assertEquals(1, newQuantity);


        verify(shoppingCartRepository, times(1)).save(shoppingCart);
    }

    @Test
    void testDecrementItemQuantity_ShouldNotDecrementIfOnlyOneLeft() {

        shoppingCart.getProducts().put(product, 1);


        shoppingCartService.decrementItemQuantity(productId, userId);


        int newQuantity = user.getShoppingCart().getProducts().get(product);
        assertEquals(1,newQuantity);

        verify(shoppingCartRepository, never()).save(any());
    }

    @Test
    void testIncrementItemQuantity_ShouldIncrementSuccessfully() {

        shoppingCartService.incrementItemQuantity(productId, userId);


        int newQuantity = user.getShoppingCart().getProducts().get(product);
        assertEquals(3,newQuantity);

        // Verify save operation
        verify(shoppingCartRepository, times(1)).save(shoppingCart);
    }
}
