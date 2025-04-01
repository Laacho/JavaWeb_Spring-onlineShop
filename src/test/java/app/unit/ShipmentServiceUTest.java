package app.unit;

import app.orders.model.Order;
import app.shipment.model.Shipment;
import app.shipment.model.Status;
import app.shipment.repository.ShipmentRepository;
import app.shipment.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShipmentServiceUTest {

    @Mock
    private ShipmentRepository shipmentRepository;


    @InjectMocks
    private ShipmentService shipmentService;

    @Test
    void testStartShipping() {
        Order order = new Order();
        order.setTotalPrice(BigDecimal.valueOf(100));

        when(shipmentRepository.save(any(Shipment.class))).thenReturn(null);

        shipmentService.startShipping(order);

        verify(shipmentRepository, times(1)).save(any(Shipment.class));


        verify(shipmentRepository).save(argThat(shipment -> {
            assertEquals(order, shipment.getOrder());
            assertEquals(Status.STARTED, shipment.getStatus());
            assertEquals(LocalDateTime.now().plusDays(5).toLocalDate(), shipment.getEstimatedDeliveryDate().toLocalDate());
            return true;
        }));
    }

    @Test
    void testFindByTrackingNumber() {
        String trackingNumber = "TRACK123";
        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNumber);

        when(shipmentRepository.findByTrackingNumber(trackingNumber)).thenReturn(Optional.of(shipment));

        Optional<Shipment> result = shipmentService.findByTrackingNumber(trackingNumber);

        assertTrue(result.isPresent());
        assertEquals(trackingNumber, result.get().getTrackingNumber());
    }
}
