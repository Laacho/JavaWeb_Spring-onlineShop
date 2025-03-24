package app.web;

import app.shipment.model.Shipment;
import app.shipment.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequestMapping("/shipment")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Autowired
    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public String viewShipmentPage() {
        return "shipmentStatus";
    }

    @GetMapping("/tracking")
    public ModelAndView tracking(@RequestParam(name = "tracking-number") String trackingNumber) {
        Optional<Shipment> optionalShipment = shipmentService.findByTrackingNumber(trackingNumber);
        ModelAndView modelAndView = new ModelAndView("shipmentStatus");
        if (optionalShipment.isPresent()) {
            modelAndView.addObject("shipment", optionalShipment.get());
            modelAndView.addObject("noShipment",false);
        } else {
            modelAndView.addObject("shipment",null);
            modelAndView.addObject("noShipment", true);
        }
        return modelAndView;
    }
}
