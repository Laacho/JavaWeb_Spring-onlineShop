package app.order_details.service;

import app.order_details.model.OrderDetails;
import app.order_details.repository.OrderDetailsRepository;
import app.products.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDetailsService {

    private final OrderDetailsRepository orderDetailsRepository;

    @Autowired
    public OrderDetailsService(OrderDetailsRepository orderDetailsRepository) {
        this.orderDetailsRepository = orderDetailsRepository;
    }


    public List<Product> getMostOrderedProducts() {
        return orderDetailsRepository.findAllByOrderByQuantityDesc().stream()
                .map(OrderDetails::getProduct)
                .distinct()
                .collect(Collectors.toList());
    }
}
