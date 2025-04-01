package app.api;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.voucher.model.Voucher;
import app.web.VoucherController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherController.class)
public class VoucherControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getRequestToAllVouchersEndpoint_shouldReturnVouchers() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = get("/vouchers")
                .with(user(principal))
                .with(csrf());

        List<Voucher> userVouchers = List.of(Voucher.builder().deadline(LocalDateTime.now()).build());
        when(userService.getById(any())).thenReturn(User.builder().vouchers(userVouchers).build());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("vouchers"));

    }

    @Test
    void getRequestToAllVouchersEndpointWithUnauthenticatedUser_shouldReturnErrorPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/vouchers");
        when(userService.getById(any())).thenReturn(User.builder().build());

        mockMvc.perform(request)
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
