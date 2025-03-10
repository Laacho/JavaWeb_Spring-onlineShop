package app.web;

import app.orders.model.Order;
import app.products.service.ProductsService;
import app.security.AuthenticationMetadata;
import app.shipment.service.ShipmentService;
import app.shopping_cart.service.ShoppingCartService;
import app.user.model.User;
import app.user.service.UserService;
import app.voucher.service.VoucherService;
import app.web.dto.ApplyVoucherRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
    private final ShipmentService shipmentService;
    private final VoucherService voucherService;

    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService, ProductsService productsService, UserService userService, ShipmentService shipmentService, VoucherService voucherService) {
        this.shoppingCartService = shoppingCartService;
        this.productsService = productsService;
        this.userService = userService;
        this.shipmentService = shipmentService;
        this.voucherService = voucherService;
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
    public ModelAndView calculate(@Valid ApplyVoucherRequest applyVoucherRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());
        if(bindingResult.hasErrors()){
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("redirect:/shopping-cart");
            modelAndView.addObject("user", user);
            return modelAndView;
        }
        BigDecimal totalAmount = shoppingCartService.applyVoucher(applyVoucherRequest, auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("redirect:/shopping-cart");
        modelAndView.addObject("totalAmount",totalAmount);
        modelAndView.addObject("user", user);
        return modelAndView;
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

    @PostMapping("/order")
    public ModelAndView order(@RequestParam BigDecimal totalAmount,@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());
        Order order = shoppingCartService.placeOrder(totalAmount, auth.getUserId());
        String trackingNumber = shipmentService.startShipping(order);
        voucherService.checkForGivingVoucher(auth.getUserId());
        //todo
        //post notification with the tracking number
        ModelAndView modelAndView = new ModelAndView("redirect:/shipment");
        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
