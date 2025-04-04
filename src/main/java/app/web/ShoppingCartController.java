package app.web;

import app.notifications.service.NotificationService;
import app.orders.model.Order;
import app.security.AuthenticationMetadata;
import app.shipment.service.ShipmentService;
import app.shopping_cart.service.ShoppingCartService;
import app.user.model.User;
import app.user.service.UserService;
import app.util.ProductUtility;
import app.voucher.service.VoucherService;
import app.web.dto.ApplyVoucherRequest;
import jakarta.servlet.http.HttpSession;
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
    private final UserService userService;
    private final ShipmentService shipmentService;
    private final VoucherService voucherService;
    private final NotificationService notificationService;
    private boolean voucherUsed;
    private String voucherCode;
    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService, UserService userService, ShipmentService shipmentService, VoucherService voucherService, NotificationService notificationService) {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
        this.shipmentService = shipmentService;
        this.voucherService = voucherService;
        this.notificationService = notificationService;
        voucherUsed= false;
    }

    @GetMapping
    public ModelAndView getShoppingCart(@RequestParam(required = false) BigDecimal totalAmount,HttpSession session,@AuthenticationPrincipal AuthenticationMetadata auth) {
        ModelAndView modelAndView = new ModelAndView("shoppingCart");
        User user = userService.getById(auth.getUserId());
        ProductUtility.sortProductsByUser(user);
        modelAndView.addObject("user", user);
        modelAndView.addObject("applyVoucherRequest",new ApplyVoucherRequest());
        if(totalAmount == null) {
            totalAmount = (BigDecimal) session.getAttribute("totalAmount");
            if(totalAmount == null) {
                totalAmount = shoppingCartService.calculateOrderSum(auth.getUserId());
            }
        }
        modelAndView.addObject("totalAmount",totalAmount);
        return modelAndView;
    }

    @PostMapping("/{id}")
    public ModelAndView addProductToShoppingCart(@PathVariable UUID id,  @AuthenticationPrincipal AuthenticationMetadata auth) {
        boolean added = shoppingCartService.addProductToCart(id, auth.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:/products/"+id+"/details?added="+added);
        return modelAndView;
    }

    @PostMapping("/{id}/remove")
    public String removeProduct(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata auth) {
        shoppingCartService.removeProduct(id,auth.getUserId());
        return "redirect:/shopping-cart";
    }
    @DeleteMapping("/clear")
    public String empty(@AuthenticationPrincipal AuthenticationMetadata auth) {
        shoppingCartService.empty(auth.getUserId());
        return "redirect:/shopping-cart";
    }
    @PutMapping("/calculate")
    public ModelAndView calculate(@Valid ApplyVoucherRequest applyVoucherRequest, BindingResult bindingResult, HttpSession session, @AuthenticationPrincipal AuthenticationMetadata auth) {
        if(bindingResult.hasErrors()){
            return new ModelAndView("shoppingCart");
        }
        BigDecimal totalAmount = shoppingCartService.applyVoucher(applyVoucherRequest, auth.getUserId());
        session.setAttribute("totalAmount", totalAmount);
        ModelAndView modelAndView = new ModelAndView("redirect:/shopping-cart");
        voucherUsed=true;
        voucherCode=applyVoucherRequest.getVoucherCode();
       // redirectAttributes.addFlashAttribute("totalAmount", totalAmount);
       modelAndView.addObject("totalAmount",totalAmount);
        return modelAndView;
    }
    @PostMapping("/{id}/decrease")
    public String decreaseItem(@PathVariable UUID id,@AuthenticationPrincipal AuthenticationMetadata auth) {
       shoppingCartService.decrementItemQuantity(id,auth.getUserId());
        return "redirect:/shopping-cart";

    }
    @PostMapping("/{id}/increase")
    public String increaseItem(@PathVariable UUID id,@AuthenticationPrincipal AuthenticationMetadata auth) {
        shoppingCartService.incrementItemQuantity(id,auth.getUserId());
        return "redirect:/shopping-cart";
    }

    @PostMapping("/order")
    public String order(@RequestParam BigDecimal totalAmount,@AuthenticationPrincipal AuthenticationMetadata auth) {
        Order order;
        if(voucherUsed){
            order = shoppingCartService.placeOrder(totalAmount, auth.getUserId(),voucherCode);
        }
        else {
            order = shoppingCartService.placeOrder(totalAmount, auth.getUserId());
        }
        String trackingNumber = shipmentService.startShipping(order);
        voucherService.checkForGivingVoucher(auth.getUserId());
        notificationService.sendNotificationWithTrackingNumber(trackingNumber,auth.getUserId());
        voucherUsed=false;
        return "redirect:/shipment";
    }


}
