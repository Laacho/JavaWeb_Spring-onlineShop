package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/shipment")
public class ShipmentController {

    private final UserService userService;

    @Autowired
    public ShipmentController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView viewShipmentPage(@AuthenticationPrincipal AuthenticationMetadata auth) {

        User user = userService.getById(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("shipmentStatus");
        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
