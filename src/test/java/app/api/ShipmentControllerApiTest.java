package app.api;

import app.orders.model.Order;
import app.security.AuthenticationMetadata;
import app.shipment.model.Carrier;
import app.shipment.model.Shipment;
import app.shipment.model.Status;
import app.shipment.service.ShipmentService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.ShipmentController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
public class ShipmentControllerApiTest {


    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ShipmentService shipmentService;
    @MockitoBean
    private UserService userService;

    @Test
    void viewShipmentPage_ShouldReturnShipmentStatusView() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = get("/shipment")
                .with(user(principal))
                .with(csrf());
        when(userService.getById(any())).thenReturn(User.builder().build());


        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("shipmentStatus"));
    }

    @Test
    void tracking_WithValidTrackingNumber_ShouldReturnShipmentDetails() throws Exception {
        String trackingNumber = "ABC123";
        Shipment shipment = Shipment.builder()
                .trackingNumber(trackingNumber)
                .status(Status.DELIVERED)
                .carrier(Carrier.EKONT)
                .order(Order.builder().orderId(UUID.randomUUID()).build())
                .build();


        when(shipmentService.findByTrackingNumber(trackingNumber)).thenReturn(Optional.of(shipment));
        when(userService.getById(any())).thenReturn(User.builder().build());
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/shipment/tracking")
                .param("tracking-number", trackingNumber)
                .with(user(principal))
                .with(csrf());


        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("shipmentStatus"))
                .andExpect(model().attribute("shipment", shipment))
                .andExpect(model().attribute("noShipment", false));
    }


    @Test
    void tracking_WithInvalidTrackingNumber_ShouldReturnNoShipment() throws Exception {
        String invalidTrackingNumber = "INVALID123";

        when(userService.getById(any())).thenReturn(User.builder().build());
        when(shipmentService.findByTrackingNumber(invalidTrackingNumber)).thenReturn(Optional.empty());
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/shipment/tracking")
                .param("tracking-number", invalidTrackingNumber)
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("shipmentStatus"))
                .andExpect(model().attribute("shipment", nullValue()))
                .andExpect(model().attribute("noShipment", true));
    }


    @Test
    void tracking_WithoutTrackingNumber_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/shipment/tracking"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
