package app.orders.service;

import app.order_details.model.OrderDetails;
import app.order_details.repository.OrderDetailsRepository;
import app.orders.model.Order;
import app.orders.repository.OrdersRepository;
import app.products.model.Product;
import app.shopping_cart.model.ShoppingCart;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final UserService userService;
    private final OrderDetailsRepository orderDetailsRepository;

    @Autowired
    public OrderService(OrdersRepository ordersRepository, UserService userService, OrderDetailsRepository orderDetailsRepository) {
        this.ordersRepository = ordersRepository;
        this.userService = userService;
        this.orderDetailsRepository = orderDetailsRepository;
    }


    public List<Order> findByUser(User user) {
        return ordersRepository.findByUser(user);
    }

    public Order placeOrder(BigDecimal totalAmount, UUID userId) {
        User user = userService.getById(userId);
        ShoppingCart shoppingCart = user.getShoppingCart();
        Map<Product, Integer> products = shoppingCart.getProducts();
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        for (Map.Entry<Product, Integer> kvp : products.entrySet()) {
            Product product = kvp.getKey();
            Integer quantity = kvp.getValue();
            OrderDetails orderDetails = OrderDetails.builder()
                    .product(product)
                    .quantity(quantity)
                    .build();
            orderDetailsList.add(orderDetails);
        }
        String description="You have successfully placed an order! Its now sent to shipment and will be arriving shortly!";
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .orderDetails(orderDetailsList)
                .description(description)
                .totalPrice(totalAmount)
                .build();
         ordersRepository.save(order);
        for (OrderDetails orderDetails : orderDetailsList) {
            orderDetails.setOrder(order);
            orderDetailsRepository.save(orderDetails);
        }
        return order;
    }
}
