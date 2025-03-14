package app.shipment.service;

import app.orders.model.Order;
import app.shipment.model.Carrier;
import app.shipment.model.Shipment;
import app.shipment.model.Status;
import app.shipment.repository.ShipmentRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;

    @Autowired
    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    public String startShipping(Order order) {
        String trackingNumber=generateTrackingNumber();
        Carrier carrier = pickRandomCarrier();
        BigDecimal price = order.getTotalPrice().add(carrier.getPrice());
        Shipment shipment = Shipment.builder()
                .order(order)
                .trackingNumber(trackingNumber)
                .carrier(carrier)
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(5))
                .status(Status.STARTED)
                .shipmentPrice(price)
                .build();
        shipmentRepository.save(shipment);
        return trackingNumber;
    }
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    private String generateTrackingNumber() {
      return   RandomStringUtils.random(5,false,true);
    }
    private Carrier pickRandomCarrier() {
        Random random = new Random();
        Carrier[] carriers = Carrier.values();
        return carriers[random.nextInt(carriers.length)];
    }
}
