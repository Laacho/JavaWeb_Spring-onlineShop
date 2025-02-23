package app.products.repository;

import app.products.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductsRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByName(String name);

    List<Product> findAllByIsAvailable(boolean available);

}
