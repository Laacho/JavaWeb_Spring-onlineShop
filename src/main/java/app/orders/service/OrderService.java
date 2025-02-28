package app.orders.service;

import app.orders.model.Order;
import app.orders.repository.OrdersRepository;
import app.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrdersRepository ordersRepository;

    @Autowired
    public OrderService(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }


    public List<Order> findByUser(User user) {
        return ordersRepository.findByUser(user);
    }
}
