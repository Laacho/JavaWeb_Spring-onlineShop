package app.unit;

import app.event.UserRegistered;
import app.exceptions.EntityNotFoundException;
import app.exceptions.InvalidUsernameOrAddressException;
import app.order_details.model.OrderDetails;
import app.orders.model.Order;
import app.products.model.Product;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;


    @InjectMocks
    private UserService userService;


    @Test
    void givenMissingUserFromDb_whenEditingProfile_thenExceptionIsThrown() {
        UUID userId = UUID.randomUUID();
        EditProfileRequest editProfileRequest = EditProfileRequest.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.editProfile(userId, editProfileRequest));
    }

    @Test
    void givenExistingUser_whenEditingProfile_thenHappyPath() {
        EditProfileRequest editProfileRequest = EditProfileRequest.builder()
                .firstName("test")
                .lastName("test")
                .email("la40@gmail.com")
                .address("Ulica Glavna")
                .profilePicture("www.image.com")
                .build();
        User user = User.builder().build();
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.editProfile(userId, editProfileRequest);

        assertEquals("test", user.getFirstName());
        assertEquals("test", user.getLastName());
        assertEquals("la40@gmail.com", user.getEmail());
        assertEquals("www.image.com", user.getProfilePicture());
        assertEquals("Ulica Glavna", user.getAddress());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenExistingInDatabase_whenGetAllUsers_thenReturnThemAll() {
        List<User> users = List.of(new User(), new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> allUsers = userService.getAllUsers();

        assertThat(allUsers).hasSize(3);
    }

    @Test
    void givenNonExistingUser_whenGettingUserByUsername_thenExceptionIsThrown() {
        String username = "non-existing-user";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getByUsername(username));
    }

    @Test
    void givenExistingUser_whenGettingUserByUsername_thenUserIsReturned() {
        String username = "Lacho";
        User user = User.builder()
                .username(username)
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThat(userService.getByUsername(username)).isNotNull();
        assertThat(userService.getByUsername(username)).isEqualTo(user);

    }

    @Test
    void givenExistingUser_whenLoadUserByUsername_thenReturnAuthenticationMetadata() {

        //Given
        String username = "Lachezar";
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .password("123123")
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        //when
        UserDetails authenticationMetadata = userService.loadUserByUsername(username);

        //Then
        assertInstanceOf(AuthenticationMetadata.class, authenticationMetadata);
        AuthenticationMetadata result = (AuthenticationMetadata) authenticationMetadata;
        assertEquals(user.getId(), result.getUserId());
        assertEquals(username, result.getUsername());
        assertEquals(user.isActive(), result.isActive());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.getRole(), result.getRole());
        assertThat(result.getAuthorities()).hasSize(1);
        assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
    }


    @Test
    void givenUserWithWantsNotifications_whenChangingNotificationStatus_thenNotificationsAreChanged() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .wantsNotifications(true)
                .build();

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        userService.changeNotifications(userId);

        assertFalse(user.isWantsNotifications());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserWithNotWantingNotifications_whenChangingNotificationStatus_thenNotificationsAreChanged() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .wantsNotifications(false)
                .build();

        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(user));

        userService.changeNotifications(userId);

        assertTrue(user.isWantsNotifications());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void givenUserWithRoleUser_whenPromotingToAdmin_thenRoleIsChanged() {
        String username = "Lacho";
        User user = User.builder()
                .role(UserRole.USER)
                .build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));


        userService.promoteToAdmin(username);

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository, times(1)).save(user);

    }

    @Test
    void givenAListOfUsersWithRoleUser_whenGettingAllByARole_thenReturnOnlyTheSpecificOne() {
        int times = 5;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            User user = User.builder()
                    .role(UserRole.USER)
                    .build();
            users.add(user);
        }
        when(userRepository.findAllByRole(any())).thenReturn(users);

        List<User> allByRole = userService.getAllByRole(UserRole.USER);

        assertThat(allByRole).hasSize(times);
    }

    @Test
    void givenAListOfUsersWithRoleAdmin_whenGettingAllByARole_thenReturnOnlyTheSpecificOne() {
        int times = 5;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            User user = User.builder()
                    .role(UserRole.ADMIN)
                    .build();
            users.add(user);
        }
        when(userRepository.findAllByRole(any())).thenReturn(users);

        List<User> allByRole = userService.getAllByRole(UserRole.ADMIN);

        assertThat(allByRole).hasSize(times);
    }

    @Test
    void givenListOfOrders_whenGettingMostOrderedProducts_toReturnThemSortedByCountInDescendingOrder() {
        Product productA = Product.builder().name("A").build();
        Product productB = Product.builder().name("B").build();
        Product productC = Product.builder().name("C").build();

        OrderDetails details1 = OrderDetails.builder().product(productA).quantity(5).build();
        OrderDetails details2 = OrderDetails.builder().product(productB).quantity(10).build();
        OrderDetails details3 = OrderDetails.builder().product(productC).quantity(2).build();
        OrderDetails details4 = OrderDetails.builder().product(productA).quantity(3).build();

        Order order1 = Order.builder().orderDetails(List.of(details1, details2)).build();
        Order order2 = Order.builder().orderDetails(List.of(details3, details4)).build();
        List<Order> orders = List.of(order1, order2);

        List<Product> mostOrderedProducts = userService.getMostOrderedProducts(orders);

        assertEquals(3, mostOrderedProducts.size());
        assertEquals(productB, mostOrderedProducts.get(0));
        assertEquals(productA, mostOrderedProducts.get(1));
        assertEquals(productC, mostOrderedProducts.get(2));
    }


    @Test
    void givenEmptyOrdersList_whenGetMostOrderedProducts_thenReturnsEmptyList() {
        // Given
        List<Order> emptyOrders = Collections.emptyList();

        // When
        List<Product> mostOrderedProducts = userService.getMostOrderedProducts(emptyOrders);

        // Then
        assertTrue(mostOrderedProducts.isEmpty());
    }

    @Test
    void givenAlreadyExistingUser_whenTryingToRegister_thenThrowException() {
        User user = User.builder().build();

        RegisterRequest request = RegisterRequest.builder()
                .username("")
                .address("")
                .password("")
                .build();
        when(userRepository.findByUsernameOrAddress(anyString(), anyString())).thenReturn(Optional.of(user));

        assertThrows(InvalidUsernameOrAddressException.class, () -> userService.register(request));
    }

    @Test
    void registeringUser_happyPath() {
        RegisterRequest request = RegisterRequest.builder()
                .username("Lacho")
                .address("Sofia")
                .password("123123")
                .build();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        when(userRepository.save(any(User.class))).thenReturn(user);

        when(userRepository.findByUsernameOrAddress(anyString(), anyString())).thenReturn(Optional.empty());
        userService.register(request);

        verify(userRepository, times(1)).save(any());
        verify(eventPublisher, times(1)).publishEvent(any(UserRegistered.class));
    }
}
