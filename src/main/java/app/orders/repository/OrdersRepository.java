package app.orders.repository;

import app.orders.model.Order;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdersRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUser(User user);
}
