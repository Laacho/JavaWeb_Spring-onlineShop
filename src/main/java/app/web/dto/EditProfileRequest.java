package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
public class EditProfileRequest {

    @Size( max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size( max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Size( max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    @URL(message = "Profile picture must be a valid image URL")
    private String profilePicture;

}
