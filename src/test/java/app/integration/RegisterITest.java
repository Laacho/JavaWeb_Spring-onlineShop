package app.integration;

import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RegisterITest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerNewUser_happyPath() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Test")
                .password("password")
                .address("address")
                .build();

        userService.register(registerRequest);
        User registeredUser = userService.getByUsername(registerRequest.getUsername());


        assertThat(registeredUser.getUsername(), is(registerRequest.getUsername()));
        assertThat(registeredUser.getAddress(), is(registerRequest.getAddress()));

        assertTrue(registeredUser.getOrders().isEmpty());
        assertTrue(registeredUser.getVouchers().isEmpty());
    }
}
