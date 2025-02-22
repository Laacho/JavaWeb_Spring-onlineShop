package app.shopping_cart.model;

import app.products.model.Product;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    private User user;

    @ElementCollection
    @CollectionTable(name = "shopping_cart_products",joinColumns = @JoinColumn(name = "shopping_cart_id"))
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    private Map<Product,Integer> products=new HashMap<>();

    @Column(nullable = false)
    private LocalDateTime addedAt;
}
