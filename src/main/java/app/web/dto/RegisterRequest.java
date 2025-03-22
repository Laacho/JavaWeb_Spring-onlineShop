package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    @NotBlank(message = "Username cant be empty")
    @Size(min = 3, max = 30,message = "Username must be between 3 and 30 characters!")
    private String username;
    @NotBlank(message = "Password cant be empty")
    @Size(min = 3, max = 30,message = "Password must be between 3 and 30 characters!")
    private String password;
    @NotBlank(message = "Address cant be empty")
    private String address;
}
