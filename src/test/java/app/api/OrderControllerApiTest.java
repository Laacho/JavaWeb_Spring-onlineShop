package app.api;

import app.orders.model.Order;
import app.orders.service.OrderService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.OrderController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

import java.util.List;
import java.util.UUID;

@WebMvcTest(OrderController.class)
public class OrderControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private  OrderService orderService;

    @MockitoBean
    private  UserService userService;

    @Test
    void getRequestToOrderHistoryEndpoint_shouldReturnOrderHistoryPage() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123","123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request=get("/orders/history")
                .with(user(principal))
                .with(csrf());
        when(userService.getById(any(UUID.class))).thenReturn(User.builder().build());
        when(orderService.findByUser(any(User.class))).thenReturn(List.of(Order.builder().build()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("orderHistory"))
                .andExpect(model().attributeExists("allOrdersForUser"));
    }

    @Test
    void getRequestToOrderHistoryEndpointWithUnauthenticatedUser_shouldReturnOrderHistoryPage() throws Exception {
        MockHttpServletRequestBuilder request=get("/orders/history");

        mockMvc.perform(request)
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
