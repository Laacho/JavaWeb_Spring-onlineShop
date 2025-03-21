package app.shopping_cart.service;

import app.exceptions.DomainException;
import app.orders.model.Order;
import app.orders.service.OrderService;
import app.products.model.Product;
import app.products.service.ProductsService;
import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.repository.ShoppingCartRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.voucher.model.Voucher;
import app.voucher.service.VoucherService;
import app.web.dto.ApplyVoucherRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShoppingCartService {

    private final UserService userService;
    private final ProductsService productsService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final VoucherService voucherService;
    private final OrderService orderService;

    @Autowired
    public ShoppingCartService(UserService userService, ProductsService productsService, ShoppingCartRepository shoppingCartRepository, VoucherService voucherService,  OrderService orderService) {
        this.userService = userService;
        this.productsService = productsService;
        this.shoppingCartRepository = shoppingCartRepository;
        this.voucherService = voucherService;
        this.orderService = orderService;
    }

    public boolean addProductToCart(UUID productId, UUID userID) {
        Product productToAdd = productsService.getById(productId);
        //check if the product the user wants to add is available in quantity
        if (!productToAdd.isAvailable() || productToAdd.getStockQuantity() <= 0) {
            return false;
        }
        User user = userService.getById(userID);
        ShoppingCart shoppingCart = user.getShoppingCart();
        Map<Product, Integer> products = shoppingCart.getProducts();
        if (products.containsKey(productToAdd)) {
            products.put(productToAdd, products.get(productToAdd) + 1);
        } else {
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

    public BigDecimal applyVoucher(ApplyVoucherRequest applyVoucherRequest, UUID userId) {
        Voucher voucher = voucherService.findByCode(applyVoucherRequest.getVoucherCode());
        UUID ownerId = voucher.getUser().getId();
        if (!ownerId.equals(userId)) {
            throw new DomainException("This voucher does not belong to you");
        }
        LocalDateTime deadline = voucher.getDeadline();
        if (deadline.isBefore(LocalDateTime.now())) {
            //invalid voucher
            throw new RuntimeException("Voucher has expired");
        }
        //valid voucher and apply it
        BigDecimal orderSum = calculateOrderSum(userId);
        BigDecimal minOrderPrice = voucher.getMinOrderPrice();
        if (orderSum.compareTo(minOrderPrice) < 0) {
            throw new RuntimeException("Minimum order price is less than to order price");
        }
        BigDecimal discountAmount = voucher.getDiscountAmount();
        return orderSum.subtract(discountAmount);
    }

    public BigDecimal calculateOrderSum(UUID userId) {
        User user = userService.getById(userId);
        Map<Product, Integer> products = user.getShoppingCart().getProducts();
        BigDecimal orderSum = BigDecimal.ZERO;
        for (Map.Entry<Product, Integer> kvp : products.entrySet()) {
            Product product = kvp.getKey();
            Integer amount = kvp.getValue();
            if (product.isOnDeal()) {
                BigDecimal discountAmount = product.getDiscountAmount();
                BigDecimal finalPrice = product.getPrice().subtract(discountAmount);
                orderSum=orderSum.add(
                        finalPrice.multiply(BigDecimal.valueOf(amount))
                );
            } else {
                orderSum = orderSum.add(product.getPrice().multiply(BigDecimal.valueOf(amount)));
            }
        }
        return orderSum;
    }

    public void sortProducts(UUID userId) {
        User user = userService.getById(userId);
        Map<Product, Integer> collect = user.getShoppingCart().getProducts()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getName())) // Sort by timestamp
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        user.getShoppingCart().setProducts(collect);
        userService.save(user);
    }

    public void decrementItemQuantity(UUID id, UUID userId) {
        User user = userService.getById(userId);
        Product product = productsService.getById(id);
        Map<Product, Integer> products = user.getShoppingCart().getProducts();
        Integer amount = products.get(product);
        if (amount == 1) {
            return;
        }
        products.put(product, products.get(product) - 1);
        ShoppingCart shoppingCart = user.getShoppingCart();
        shoppingCart.setProducts(products);
        shoppingCartRepository.save(shoppingCart);

    }

    public void incrementItemQuantity(UUID id, UUID userId) {
        Product product = productsService.getById(id);
        User user = userService.getById(userId);
        Map<Product, Integer> products = user.getShoppingCart().getProducts();
        products.put(product, products.get(product) + 1);
        ShoppingCart shoppingCart = user.getShoppingCart();
        shoppingCart.setProducts(products);
        shoppingCartRepository.save(shoppingCart);
    }

    public Order placeOrder(BigDecimal totalAmount, UUID userId, String voucherCode) {
        if (!checkIfPriceMatches(userId, totalAmount,voucherCode)) {
            throw new DomainException("Price do not match");
        }
        Order order = orderService.placeOrder(totalAmount, userId);
        empty(userId);
        return order;
    }
    public Order placeOrder(BigDecimal totalAmount, UUID userId) {
        if (!checkIfPriceMatches(userId,totalAmount)) {
            throw new DomainException("Price do not match");
        }
        Order order = orderService.placeOrder(totalAmount, userId);
        empty(userId);
        return order;
    }
    public ShoppingCart initShoppingCart(User user) {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .user(user)
                .products(new LinkedHashMap<>())
                .addedAt(LocalDateTime.now())
                .build();
        return shoppingCartRepository.save(shoppingCart);
    }
    private boolean checkIfPriceMatches(UUID userId, BigDecimal totalAmount, String voucherCode) {
        ApplyVoucherRequest applyVoucherRequest=new ApplyVoucherRequest();
        applyVoucherRequest.setVoucherCode(voucherCode);

        BigDecimal finalAmount = applyVoucher(applyVoucherRequest, userId);

        return totalAmount.compareTo(finalAmount) == 0;
    }
    private boolean checkIfPriceMatches(UUID userId,  BigDecimal totalAmount) {
        BigDecimal recalculatedTotal = calculateOrderSum(userId);
        return totalAmount.compareTo(recalculatedTotal) == 0;
    }


}
