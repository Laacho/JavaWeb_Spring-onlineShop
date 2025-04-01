package app.integration;

import app.order_details.model.OrderDetails;
import app.orders.model.Order;
import app.orders.repository.OrdersRepository;
import app.orders.service.OrderService;
import app.products.model.Category;
import app.products.model.Product;
import app.products.repository.ProductsRepository;
import app.products.service.ProductsService;
import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.repository.ShoppingCartRepository;
import app.shopping_cart.service.ShoppingCartService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.voucher.model.Voucher;
import app.voucher.repository.VoucherRepository;
import app.voucher.service.VoucherService;
import app.web.dto.AddAProductRequest;
import app.web.dto.RegisterRequest;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PlaceOrderITest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private ProductsRepository productsRepository;
    @Autowired
    private ProductsService productsService;

    private User testUser;
    private List<Product> productList;
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private VoucherRepository voucherRepository;

    @BeforeEach
    void initUser_andProducts() {
        //register user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Test")
                .password("123123")
                .address("address")
                .build();
        userService.register(registerRequest);
        testUser = userService.getByUsername("Test");
        //init a product
        AddAProductRequest addAProductRequest = new AddAProductRequest();
        addAProductRequest.setProductName("Test Product");
        addAProductRequest.setProductImageURL("www.image.com");
        addAProductRequest.setPrice(BigDecimal.valueOf(100));
        addAProductRequest.setQuantityPerUnit(10);
        addAProductRequest.setStockQuantity(100);
        addAProductRequest.setCategory(Category.OTHER);
        addAProductRequest.setDescription("Test Description");
        productsService.addAProduct(addAProductRequest);
        productList = productsService.findAll();

        User refreshedUser = userService.getById(testUser.getId());
        ShoppingCart cart = refreshedUser.getShoppingCart();
        assertNotNull(cart);
        Hibernate.initialize(cart.getProducts());
        assertTrue(cart.getProducts().isEmpty());
    }

    @Test
    @Transactional
    void placeOrder_happyPath() {
        shoppingCartService.addProductToCart(productList.get(0).getId(), testUser.getId());


        BigDecimal totalAmount = BigDecimal.valueOf(100);
        UUID userId = testUser.getId();
        Order order = shoppingCartService.placeOrder(totalAmount, userId);

        // Then
        assertNotNull(order);
        assertEquals(userId, order.getUser().getId());
        assertEquals(totalAmount, order.getTotalPrice());
        assertEquals(1, order.getOrderDetails().size());

        OrderDetails orderDetails = order.getOrderDetails().get(0);
        assertEquals("Test Product", orderDetails.getProduct().getName());
        assertEquals(1, orderDetails.getQuantity());

        // Ensure order is saved in the database
        Optional<Order> savedOrder = ordersRepository.findById(order.getOrderId());
        assertTrue(savedOrder.isPresent());
    }

    @Test
    @Transactional
    void placeOrderWithVoucher_happyPath() {
        AddAProductRequest addAProductRequest = new AddAProductRequest();
        addAProductRequest.setProductName("Test Product2");
        addAProductRequest.setProductImageURL("www.image.com");
        addAProductRequest.setPrice(BigDecimal.valueOf(150));
        addAProductRequest.setQuantityPerUnit(10);
        addAProductRequest.setStockQuantity(100);
        addAProductRequest.setCategory(Category.OTHER);
        addAProductRequest.setDescription("Test Description");
        productsService.addAProduct(addAProductRequest);
        productList = productsService.findAll();
        for (Product product : productList) {
            shoppingCartService.addProductToCart(product.getId(), testUser.getId());
        }


        String code = "123123";
        Voucher voucher = Voucher.builder()
                .code(code)
                .minOrderPrice(BigDecimal.valueOf(50.00))
                .discountAmount(BigDecimal.valueOf(10.00))
                .deadline(LocalDateTime.now().plusDays(30))
                .user(testUser)
                .build();
        voucherRepository.save(voucher);

        BigDecimal totalAmount = BigDecimal.valueOf(240);
        UUID userId = testUser.getId();
        Order order = shoppingCartService.placeOrder(totalAmount, userId, code);

        // Then
        assertNotNull(order);
        assertEquals(userId, order.getUser().getId());
        assertEquals(totalAmount, order.getTotalPrice());
        assertEquals(2, order.getOrderDetails().size());

        OrderDetails orderDetails = order.getOrderDetails().get(0);
        assertEquals("Test Product", orderDetails.getProduct().getName());
        assertEquals(1, orderDetails.getQuantity());


        OrderDetails orderDetails2 = order.getOrderDetails().get(1);
        assertEquals("Test Product2", orderDetails2.getProduct().getName());
        assertEquals(1, orderDetails2.getQuantity());

        Optional<Order> savedOrder = ordersRepository.findById(order.getOrderId());
        assertTrue(savedOrder.isPresent());
    }
}
