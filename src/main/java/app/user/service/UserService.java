package app.user.service;

import app.cloudinary.CloudinaryService;
import app.exceptions.DomainException;
import app.order_details.model.OrderDetails;
import app.orders.model.Order;
import app.products.model.Product;
import app.security.AuthenticationMetadata;
import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.repository.ShoppingCartRepository;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ShoppingCartRepository shoppingCartRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.shoppingCartRepository = shoppingCartRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public void register(RegisterRequest registerRequest) {
        Optional<User> optionalUser = userRepository.findByUsernameOrAddress(registerRequest.getUsername(),registerRequest.getAddress());
        if (optionalUser.isPresent()) {
            throw new DomainException("Username or password are incorrect");
        }
        User user = initializeDefaultUser(registerRequest);
        userRepository.save(user);

        ShoppingCart shoppingCart = initShoppingCart(user);

        shoppingCartRepository.save(shoppingCart);
        user.setShoppingCart(shoppingCart);
        userRepository.save(user);
    }
    public User getById(UUID id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new DomainException("User with id [%s] does not exist.".formatted(id)));
    }

    public List<User> getAllByRole(UserRole role) {
       return userRepository.findAllByRole(role);
    }
//    @Cacheable(value = "users",key = "#id")
//    public List<User> allUsers(){
//        return userRepository.findAll();
//    }
//    @CachePut(value = "users", key = "#user.id")
//    public User updateUser(User user) {
//        System.out.println("Updating user...");
//      //  database.put(user.getId(), user);
//        return user;
//    }
//
//    @CacheEvict(value = "users", key = "#id")
//    public void deleteUser(UUID id) {
//
//        userRepository.deleteById(id);
//    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new DomainException("User with this username does not exist."));
        return new AuthenticationMetadata(user.getId(),username, user.getPassword(), user.getRole(), user.isActive());
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

        if(editProfileRequest.getProfilePicture() != null &&
                !editProfileRequest.getProfilePicture().isBlank()
            && !editProfileRequest.getProfilePicture().contains("res.cloudinary.com")
        ){

            String stringCompletableFuture = uploadToCloud(editProfileRequest.getProfilePicture());
            user.setProfilePicture(stringCompletableFuture);
        }
        else{
            user.setProfilePicture(editProfileRequest.getProfilePicture());
        }
        user.setAddress(editProfileRequest.getAddress());
        user.setEmail(editProfileRequest.getEmail());
        user.setFirstName(editProfileRequest.getFirstName());
        user.setLastName(editProfileRequest.getLastName());

        userRepository.save(user);
    }

    private String uploadToCloud(String productImageURL)  {
            try {
                return cloudinaryService.uploadFromUrlToUserProfiles(productImageURL);
            }catch (IOException e){
                return productImageURL;
            }
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
                .updatedAt(now)
                .build();
    }

    private ShoppingCart initShoppingCart(User user) {
        return ShoppingCart.builder()
                .user(user)
                .products(new HashMap<>())
                .addedAt(LocalDateTime.now())
                .build();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void switchStatus(UUID id) {
        User user = getById(id);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public void switchRole(UUID id) {
        User user = getById(id);
        if(user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ADMIN);
        }
        else{
            user.setRole(UserRole.USER);
        }
        userRepository.save(user);
    }
}
