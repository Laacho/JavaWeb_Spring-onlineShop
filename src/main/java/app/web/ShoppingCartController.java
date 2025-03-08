package app.web;

import app.products.service.ProductsService;
import app.security.AuthenticationMetadata;
import app.shopping_cart.service.ShoppingCartService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ApplyVoucherRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
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
        shoppingCartService.sortProducts(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("shoppingCart");
        User user = userService.getById(auth.getUserId());
        modelAndView.addObject("user", user);
        modelAndView.addObject("applyVoucherRequest",new ApplyVoucherRequest());
        BigDecimal totalAmount = shoppingCartService.calculateOrderSum(auth.getUserId());
        modelAndView.addObject("totalAmount",totalAmount);
        return modelAndView;
    }

    @PostMapping("/{id}")
    public ModelAndView addProductToShoppingCart(@PathVariable UUID id,  @AuthenticationPrincipal AuthenticationMetadata auth) {
        boolean added = shoppingCartService.addProductToCart(id, auth.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/products/"+id+"/details?added="+added);
        //maybe this: "redirect:/products/"+id+"/details"
       // modelAndView.addObject("added", added);
        //todo finish logic when adding product
        //i mean displaying the label that is green
        return modelAndView;
    }

    @PostMapping("/{id}/remove")
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
    @PutMapping("/calculate")
    public ModelAndView calculate(@Valid ApplyVoucherRequest applyVoucherRequest, @AuthenticationPrincipal AuthenticationMetadata auth) {
        shoppingCartService.applyVoucher(applyVoucherRequest,auth.getUserId());
        //todo finish
        return null;
    }
    @PostMapping("/{id}/decrease")
    public ModelAndView decreaseItem(@PathVariable UUID id,@AuthenticationPrincipal AuthenticationMetadata auth) {
       shoppingCartService.decrementItemQuantity(id,auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("redirect:/shopping-cart");
        User user = userService.getById(auth.getUserId());
        modelAndView.addObject("user", user);
        return modelAndView;

    }
    @PostMapping("/{id}/increase")
    public ModelAndView increaseItem(@PathVariable UUID id,@AuthenticationPrincipal AuthenticationMetadata auth) {
        shoppingCartService.incrementItemQuantity(id,auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("redirect:/shopping-cart");
        User user = userService.getById(auth.getUserId());
        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
