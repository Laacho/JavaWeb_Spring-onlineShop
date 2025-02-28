package app.web.mapper;

import app.user.model.User;
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
}
