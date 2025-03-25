package app.user.service;

import app.user.model.User;
import app.user.model.UserRole;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class UserInit implements CommandLineRunner {

    private final UserService userService;

    @Autowired
    public UserInit(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        //checking for min 1 admin
        List<User> allByRole = userService.getAllByRole(UserRole.ADMIN);
        if(allByRole.isEmpty()) {
            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username("admin")
                    .password("admin")
                    .address("ONLINE SHOP CENTRAL")
                    .build();
            userService.register(registerRequest);
            userService.promoteToAdmin("admin");

       }
    }
}
