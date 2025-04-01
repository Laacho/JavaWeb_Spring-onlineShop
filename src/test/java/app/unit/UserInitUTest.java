package app.unit;

import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserInit;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserInitUTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserInit userInit;


    @Test
    void test(){
        List<User> list = List.of(User.builder().role(UserRole.ADMIN).build());
        when(userService.getAllByRole(any(UserRole.class))).thenReturn(list);

        userInit.run();

        verify(userService,never()).register(any());
        verify(userService,never()).promoteToAdmin(any());

    }
    @Test
    void givenNoAdminExists_whenRun_thenAdminIsCreatedAndPromoted() {
        when(userService.getAllByRole(UserRole.ADMIN)).thenReturn(Collections.emptyList());

        userInit.run();

        verify(userService, times(1)).register(
                argThat(request ->
                        request.getUsername().equals("admin") &&
                                request.getPassword().equals("admin") &&
                                request.getAddress().equals("ONLINE SHOP CENTRAL")
                )
        );


        verify(userService, times(1)).promoteToAdmin("admin");
    }
}
