package app.scheduling_upload.service;

import app.cloudinary.CloudinaryService;
import app.products.model.Product;
import app.products.repository.ProductsRepository;
import app.user.model.User;
import app.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScheduledService {
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final ProductsRepository productsRepository;
    private static final String CONTAINING_LINK = "res.cloudinary.com";

    @Autowired
    public ScheduledService(CloudinaryService cloudinaryService, UserRepository userRepository, ProductsRepository productsRepository) {
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
        this.productsRepository = productsRepository;
    }


    //    @Scheduled(cron = "@hourly")
    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduled() {
        //finds users or products with photos not uploaded to the cloud
        checkForUnUploadedUserProfilePictures();
        checkForUnUploadedProductPictures();
    }

    private void checkForUnUploadedProductPictures() {
        List<Product> productsWithNoCloudUrl = productsRepository.findByPhotoNotContaining(CONTAINING_LINK);
        if (productsWithNoCloudUrl.isEmpty()) {
            return;
        }

        List<Product> updatedProducts = new ArrayList<>();
        for (Product product : productsWithNoCloudUrl) {
            String photoURL = product.getPhoto();
            try {
                String cloudURL = cloudinaryService.uploadFromUrlToProducts(photoURL);
                product.setPhoto(cloudURL);
                updatedProducts.add(product);
            } catch (IOException e) {
                log.error("Failed to upload photo for product " + product.getId());
            }
        }
        productsRepository.saveAll(updatedProducts);
    }

    private void checkForUnUploadedUserProfilePictures() {
        List<User> usersWithNoCloudUrl = userRepository.findByProfilePictureNotContaining(CONTAINING_LINK);
        if (usersWithNoCloudUrl.isEmpty()) {
            return;
        }
        List<User> updatedUsers = new ArrayList<>();
        for (User user : usersWithNoCloudUrl) {
            String profilePicture = user.getProfilePicture();
            try {
                String cloudURL = cloudinaryService.uploadFromUrlToUserProfiles(profilePicture);
                user.setProfilePicture(cloudURL);
                updatedUsers.add(user);
            } catch (IOException e) {
                log.error("Failed to upload profile picture for user " + user.getId());
            }
        }
        userRepository.saveAll(updatedUsers);
    }
}
