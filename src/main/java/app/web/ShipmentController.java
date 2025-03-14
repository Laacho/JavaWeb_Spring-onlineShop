package app.web;

import app.security.AuthenticationMetadata;
import app.shipment.model.Shipment;
import app.shipment.service.ShipmentService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/shipment")
public class ShipmentController {

    private final UserService userService;
    private final ShipmentService shipmentService;

    @Autowired
    public ShipmentController(UserService userService, ShipmentService shipmentService) {
        this.userService = userService;
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public ModelAndView viewShipmentPage(@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("shipmentStatus");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("/tracking")
    public ModelAndView tracking(@RequestParam(name = "tracking-number") String trackingNumber,@AuthenticationPrincipal AuthenticationMetadata auth) {
        Optional<Shipment> optionalShipment = shipmentService.findByTrackingNumber(trackingNumber);
        ModelAndView modelAndView = new ModelAndView("shipmentStatus");
        User user = userService.getById(auth.getUserId());
        modelAndView.addObject("user", user);
        if (optionalShipment.isPresent()) {
            modelAndView.addObject("shipment", optionalShipment.get());
            modelAndView.addObject("noShipment",false);
        } else {
            modelAndView.addObject("noShipment", true);
            modelAndView.addObject("shipment",null);
        }
        return modelAndView;
    }
}
