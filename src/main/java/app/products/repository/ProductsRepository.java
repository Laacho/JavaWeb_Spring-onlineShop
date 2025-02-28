package app.products.repository;

import app.products.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductsRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByName(String name);

    List<Product> findAllByIsAvailable(boolean available);
    List<Product> findAllByNameContainingIgnoreCase(String name);

    List<Product> findByIsOnDeal(boolean onDeal);

    List<Product> findByPhotoNotContaining(String photo);
    List<Product> findByNameContainingIgnoreCase(String productName);
    @Modifying
    @Query("UPDATE Product p SET p.isOnDeal = false")
    void removeAllDeals();
}
