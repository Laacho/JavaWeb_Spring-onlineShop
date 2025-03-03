package app.web.mapper;

import app.products.model.Product;
import app.user.model.User;
import app.web.dto.EditProductDetails;
import app.web.dto.EditProfileRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DTOMapper {

    public static EditProfileRequest mapUserToUserEditRequest(User user) {
        return EditProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .address(user.getAddress())
                .build();
    }

    public static EditProductDetails mapProductToProductEditDetails(Product product) {
        return EditProductDetails.builder()
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(product.getCategory())
                .description(product.getDescription())
                .available(product.isAvailable())
                .image(product.getPhoto())
                .build();
    }
}
