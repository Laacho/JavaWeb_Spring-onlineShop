package app.order_details.repository;

import app.order_details.model.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, UUID> {
    List<OrderDetails> findAllByOrderByQuantityDesc();
}
