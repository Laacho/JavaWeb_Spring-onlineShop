package app.web;

import app.products.model.Product;
import app.products.service.ProductsService;
import app.security.AuthenticationMetadata;
import app.shopping_cart.service.ShoppingCartService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/shopping-cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;
    private final ProductsService productsService;
    private final UserService userService;

    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService, ProductsService productsService, UserService userService) {
        this.shoppingCartService = shoppingCartService;
        this.productsService = productsService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getShoppingCart(@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("shoppingCart");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping("/{id}/{added}")
    public ModelAndView addProductToShoppingCart(@PathVariable UUID id, @PathVariable boolean added, @AuthenticationPrincipal AuthenticationMetadata auth) {
        added = shoppingCartService.addProductToCart(id, auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("redirect:/products/"+id+"/details");
        modelAndView.addObject("added", added);
        //todo finish logic when adding product
        return modelAndView;
    }

    @DeleteMapping("/{id}/remove")
    public ModelAndView removeProduct(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata auth) {
        shoppingCartService.removeProduct(id,auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("redirect:/shopping-cart");
        User user = userService.getById(auth.getUserId());
        modelAndView.addObject("user", user);

        return modelAndView;
    }
    @DeleteMapping("/clear")
    public ModelAndView empty(@AuthenticationPrincipal AuthenticationMetadata auth) {
        shoppingCartService.empty(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("redirect:/shopping-cart");
        User user = userService.getById(auth.getUserId());
        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
