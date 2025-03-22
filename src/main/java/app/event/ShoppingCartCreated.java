package app.event;

import app.shopping_cart.model.ShoppingCart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartCreated {
    private ShoppingCart shoppingCart;
}
