package app.user.service;

import app.event.ShoppingCartCreated;
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
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }
//na interview kaji sa circular dependency
    @Transactional
    public void register(RegisterRequest registerRequest) {
        Optional<User> optionalUser = userRepository.findByUsernameOrAddress(registerRequest.getUsername(), registerRequest.getAddress());
        if (optionalUser.isPresent()) {
            throw new InvalidUsernameOrAddressException("Username or password are incorrect");
        }
        User user = initializeDefaultUser(registerRequest);
        userRepository.save(user);
        UserRegistered userRegistered = new UserRegistered(user);
        eventPublisher.publishEvent(userRegistered);
    }
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id [%s] does not exist.".formatted(id)));
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with name [%s] does not exist.".formatted(username)));
    }

    public List<User> getAllByRole(UserRole role) {
        return userRepository.findAllByRole(role);
    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getByUsername(username);
        return new AuthenticationMetadata(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }

    public List<Product> getMostOrderedProducts(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getOrderDetails().stream())
                .collect(Collectors.groupingBy(
                        OrderDetails::getProduct,
                        Collectors.summingInt(OrderDetails::getQuantity)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void editProfile(UUID userId, EditProfileRequest editProfileRequest) {
        User user = getById(userId);
       if(StringUtils.hasText(editProfileRequest.getAddress())) {
           user.setAddress(editProfileRequest.getAddress());
       }
       if(StringUtils.hasText(editProfileRequest.getEmail())) {
           user.setEmail(editProfileRequest.getEmail());
       }
        user.setFirstName(editProfileRequest.getFirstName());
        user.setLastName(editProfileRequest.getLastName());
        user.setProfilePicture(editProfileRequest.getProfilePicture());
        userRepository.save(user);

    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void switchStatus(UUID id) {
        User user = getById(id);
        user.setActive(!user.isActive());
        userRepository.save(user);

        updateSecurityContext(user);
    }

    public void switchRole(UUID id) {
        User user = getById(id);
        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }
        userRepository.save(user);
        updateSecurityContext(user);
    }

    public long getOrdersForUser(UUID userId) {
        return userRepository.countByOrders_UserId(userId);
    }

    public void changeNotifications(UUID userId) {
        User user = getById(userId);
        boolean wantsNotifications = user.isWantsNotifications();
        user.setWantsNotifications(!wantsNotifications);
        userRepository.save(user);
    }
    public void promoteToAdmin(String admin) {
        User user = getByUsername(admin);
        user.setRole(UserRole.ADMIN);
        userRepository.save(user);
    }
    @EventListener
    protected void handleShoppingCartCreated(ShoppingCartCreated shoppingCartCreated){
        User user = getById(shoppingCartCreated.getShoppingCart().getUser().getId());
        user.setShoppingCart(shoppingCartCreated.getShoppingCart());
        userRepository.save(user);
    }
    private void updateSecurityContext(User user) {
        UserDetails updatedUser = loadUserByUsername(user.getUsername());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                updatedUser, authentication.getCredentials(), updatedUser.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(newAuth);
        SecurityContextHolder.clearContext(); // Clears old authentication
        SecurityContextHolder.getContext().setAuthentication(newAuth);

    }

    private User initializeDefaultUser(RegisterRequest registerRequest) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .address(registerRequest.getAddress())
                .role(UserRole.USER)
                .isActive(true)
                .createdAt(now)
                .wantsNotifications(true)
                .updatedAt(now)
                .build();
    }


}
