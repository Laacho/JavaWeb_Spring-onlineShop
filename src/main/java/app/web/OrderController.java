package app.web;


import app.orders.model.Order;
import app.orders.service.OrderService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }
    @GetMapping("/history")
    public ModelAndView getOrderHistory(@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());

        List<Order> allOrdersForUser = orderService.findByUser(user);
        ModelAndView modelAndView = new ModelAndView("orderHistory");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allOrdersForUser", allOrdersForUser);
        return modelAndView;
    }
}
