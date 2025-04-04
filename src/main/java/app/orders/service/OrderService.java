package app.orders.service;

import app.order_details.model.OrderDetails;
import app.order_details.repository.OrderDetailsRepository;
import app.orders.model.Order;
import app.orders.repository.OrdersRepository;
import app.products.model.Product;
import app.products.service.ProductsService;
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
    private final ProductsService productsService;

    @Autowired
    public OrderService(OrdersRepository ordersRepository, UserService userService, OrderDetailsRepository orderDetailsRepository, ProductsService productsService) {
        this.ordersRepository = ordersRepository;
        this.userService = userService;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productsService = productsService;
    }


    public List<Order> findByUser(User user) {
        return ordersRepository.findByUser(user);
    }

    public Order placeOrder(BigDecimal totalAmount, UUID userId) {
        User user = userService.getById(userId);
        Map<Product, Integer> products = user.getShoppingCart().getProducts();
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
        productsService.reduceItemQuantity(orderDetailsList);
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
